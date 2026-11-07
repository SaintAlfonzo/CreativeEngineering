global.protoA = Object.prototype; global.jobjA = jobj; global.walk = function () { return null }
var H = {
	f(arr) { const out = []; for (let k = 0; k < arr.length; k++) { let s = String(arr[k]); let c = s.charAt(0); let keep = true; if (k == 0) keep = false; out.push(s + c + keep) } return out }
}
throw new Error('RESULT ' + H.f(['a1', 'b2', 'c3']).join(','))
