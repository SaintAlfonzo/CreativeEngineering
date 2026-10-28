"""Minimal Minecraft RCON client for the perf suite (feature_writeup/perf_suite.md 3.2).

The server runs each command on its main thread and sends the output back, so a call returns when the command is
done (a `save-all flush` round trip is the flush time). A dead server is a refused or reset connection, never a hang
(the stdin fifo of scratch_server.sh blocks forever once nobody reads it).
"""
import socket
import struct

AUTH, COMMAND, RESPONSE = 3, 2, 0
# RconClient reads into a 1460-byte buffer: longer commands are cut off by the server
MAX_COMMAND = 1400


class RconError(Exception):
    pass


class Rcon:
    def __init__(self, host, port, password, timeout=300.0):
        self.addr = (host, port)
        self.password = password
        self.timeout = timeout
        self.sock = None
        self.next_id = 1

    def connect(self, connect_timeout=5.0):
        self.close()
        s = socket.create_connection(self.addr, timeout=connect_timeout)
        s.settimeout(self.timeout)
        self.sock = s
        rid = self._send(AUTH, self.password)
        got, _ = self._recv()
        if got == -1 or got != rid:
            self.close()
            raise RconError("rcon auth refused")

    def close(self):
        if self.sock:
            try:
                self.sock.close()
            except OSError:
                pass
        self.sock = None

    def _send(self, kind, body):
        rid = self.next_id
        self.next_id += 1
        data = body.encode("utf-8")
        pkt = struct.pack("<ii", rid, kind) + data + b"\x00\x00"
        self.sock.sendall(struct.pack("<i", len(pkt)) + pkt)
        return rid

    def _read_exact(self, n):
        buf = b""
        while len(buf) < n:
            chunk = self.sock.recv(n - len(buf))
            if not chunk:
                raise ConnectionResetError("rcon connection closed")
            buf += chunk
        return buf

    def _recv(self):
        (length,) = struct.unpack("<i", self._read_exact(4))
        payload = self._read_exact(length)
        rid, _kind = struct.unpack("<ii", payload[:8])
        return rid, payload[8:-2].decode("utf-8", "replace")

    def command(self, cmd, timeout=None):
        """Runs one console command; returns its output. Raises OSError / RconError on a dead connection."""
        if len(cmd) > MAX_COMMAND:
            raise RconError(f"command longer than {MAX_COMMAND} chars: {cmd[:60]}...")
        if self.sock is None:
            self.connect()
        self.sock.settimeout(timeout or self.timeout)
        self._send(COMMAND, cmd)
        _, text = self._recv()
        # outputs over 4096 bytes come in several packets: take what follows within a short gap
        self.sock.settimeout(0.2)
        try:
            while True:
                _, more = self._recv()
                text += more
        except (socket.timeout, BlockingIOError):
            pass
        finally:
            self.sock.settimeout(self.timeout)
        return text
