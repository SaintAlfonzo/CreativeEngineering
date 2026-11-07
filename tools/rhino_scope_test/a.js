global.protoA = Object.prototype
global.jobjA = jobj
global.walk = function walk(v, path) {
  if (v === undefined || v === null) return path
  if (typeof v !== 'object') return null
  if (Array.isArray(v)) {
    for (var n = 0; n < v.length; n++) { var a = walk(v[n], path + '[' + n + ']'); if (a) return a }
    return null
  }
  if (Object.prototype.toString.call(v) !== '[object Object]') return null
  var keys = Object.keys(v)
  for (var k = 0; k < keys.length; k++) { var b = walk(v[keys[k]], path + '.' + keys[k]); if (b) return b }
  return null
}
