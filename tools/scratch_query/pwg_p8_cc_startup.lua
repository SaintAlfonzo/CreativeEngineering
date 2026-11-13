-- Scratch-only CC: Tweaked startup program for the Create: Power Grid port, phase P8 row SQ-PWG-66 (PWG-40). Copy to
-- <world>/computercraft/computer/<id>/startup.lua of a scratch server before the computer is placed with
-- `setblock ... computercraft:computer_advanced{ComputerId:<id>,On:1b}`; never into the pack. Writes pwg_out.txt next
-- to itself: every attached peripheral with its type and getter values (three samples, 2 s apart), then
-- setValue(7) / clearValue on a Power Grid redstone converter if one is attached, with a 10 s window in between.
local out = fs.open("pwg_out.txt", "w")
local function log(s)
  out.writeLine(s)
  out.flush()
end
local getters = { "voltage", "current", "power", "energy", "getValue", "maxRange", "rangePercentage",
  "capacity", "chargePercentage", "powerDraw", "mode", "load", "rpm" }
sleep(2)
log("start clock=" .. os.clock())
for sample = 1, 3 do
  local names = peripheral.getNames()
  log("sample " .. sample .. " peripherals=" .. #names)
  for _, side in ipairs(names) do
    local p = peripheral.wrap(side)
    local line = side .. " type=" .. tostring(peripheral.getType(side))
    for _, m in ipairs(getters) do
      if p[m] then
        local ok, v = pcall(p[m])
        line = line .. " " .. m .. "=" .. (ok and tostring(v) or ("error " .. tostring(v)))
      end
    end
    log(line)
  end
  sleep(2)
end
for _, side in ipairs(peripheral.getNames()) do
  if peripheral.getType(side) == "powergrid_redstone_converter" then
    local p = peripheral.wrap(side)
    local ok, err = pcall(p.setValue, 7)
    log(side .. " setValue(7) ok=" .. tostring(ok) .. (ok and "" or (" " .. tostring(err))) .. " clock=" .. os.clock())
    sleep(10)
    ok, err = pcall(p.clearValue)
    log(side .. " clearValue ok=" .. tostring(ok) .. (ok and "" or (" " .. tostring(err))) .. " clock=" .. os.clock())
  end
end
log("end")
out.close()
