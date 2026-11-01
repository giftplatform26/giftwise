export function memoryIndex(index,direction,total){return total>0?((index+direction)%total+total)%total:0;}
export function memoryDuration(message,seconds=10){const words=String(message||'').trim().split(/\s+/).filter(Boolean).length;return Math.max(seconds,Math.min(45,Math.ceil(words/3)+3))*1000;}
export function mergeMemories(current,incoming){const seen=new Set(current.map(memory=>memory.id));return [...current,...incoming.filter(memory=>{if(seen.has(memory.id))return false;seen.add(memory.id);return true})];}
