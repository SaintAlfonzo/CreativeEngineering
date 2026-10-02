#!/usr/bin/env python3
"""TCP proxy that adds one-way latency in both directions, for the item-loss repro.

    python3 lagproxy.py LISTEN_PORT TARGET_PORT CONTROL_FILE

CONTROL_FILE holds "<delay_ms> [jitter_ms]" and is re-read every half second, so the
latency can be changed while a client is connected. Bytes are released in order (FIFO
per direction), so jitter never reorders the stream, just like real TCP.
"""
import asyncio
import random
import sys
import time

LISTEN, TARGET, CONTROL = int(sys.argv[1]), int(sys.argv[2]), sys.argv[3]
_cache = {"t": 0.0, "delay": 0.0, "jitter": 0.0}


def latency():
    now = time.monotonic()
    if now - _cache["t"] > 0.5:
        _cache["t"] = now
        try:
            parts = open(CONTROL).read().split()
            _cache["delay"] = float(parts[0]) / 1000 if parts else 0.0
            _cache["jitter"] = float(parts[1]) / 1000 if len(parts) > 1 else 0.0
        except (OSError, ValueError):
            pass
    return _cache["delay"] + random.random() * _cache["jitter"]


async def pipe(reader, writer):
    loop = asyncio.get_running_loop()
    queue = asyncio.Queue()

    async def sender():
        while True:
            due, data = await queue.get()
            wait = due - loop.time()
            if wait > 0:
                await asyncio.sleep(wait)
            if data is None:
                writer.close()
                return
            writer.write(data)
            await writer.drain()

    task = asyncio.create_task(sender())
    try:
        while True:
            data = await reader.read(65536)
            queue.put_nowait((loop.time() + latency(), data or None))
            if not data:
                break
    except (ConnectionError, OSError):
        queue.put_nowait((loop.time(), None))
    await task


async def handle(client_reader, client_writer):
    server_reader, server_writer = await asyncio.open_connection("127.0.0.1", TARGET)
    print(f"[lagproxy] connection opened, delay file {CONTROL}", flush=True)
    await asyncio.gather(pipe(client_reader, server_writer), pipe(server_reader, client_writer),
                         return_exceptions=True)
    print("[lagproxy] connection closed", flush=True)


async def main():
    server = await asyncio.start_server(handle, "127.0.0.1", LISTEN)
    print(f"[lagproxy] 127.0.0.1:{LISTEN} -> 127.0.0.1:{TARGET}", flush=True)
    async with server:
        await server.serve_forever()


asyncio.run(main())
