// Runs only inside the configured first-party origin. No credentials cross the bridge.
export function nativeBridge(origin: string) {
  return `(function(){
    if(location.origin!==${JSON.stringify(origin)} || window.__giftwiseBridge) return;
    window.__giftwiseBridge=true;
    function send(data){window.ReactNativeWebView.postMessage(JSON.stringify(data));}
    function problem(){window.alert('This file could not be exported. Try a smaller file or use the browser version.');}
    function exportFile(blob,name){
      if(blob.size>12000000){problem();return;}
      var reader=new FileReader();
      reader.onload=function(){send({type:'export',name:name||'GiftWise-export',mime:(blob.type||'application/octet-stream').split(';')[0],base64:String(reader.result).split(',')[1]});};
      reader.onerror=problem;reader.readAsDataURL(blob);
    }
    function handleDownload(link){
      if(!link || !link.hasAttribute('download')) return false;
      var href=link.href;
      if(href.startsWith('blob:') || href.startsWith('data:') || new URL(href,location.href).origin===location.origin){
        fetch(href,{credentials:'same-origin'}).then(function(r){if(!r.ok)throw Error();return r.blob();}).then(function(blob){exportFile(blob,link.download);}).catch(problem);return true;
      }
      return false;
    }
    var originalClick=HTMLAnchorElement.prototype.click;
    HTMLAnchorElement.prototype.click=function(){if(!handleDownload(this))return originalClick.call(this);};
    document.addEventListener('click',function(event){var link=event.target && event.target.closest ? event.target.closest('a[download]') : null;if(handleDownload(link))event.preventDefault();},true);
    try{Object.defineProperty(navigator,'share',{configurable:true,value:async function(data){if(data.files && data.files.length){for(var file of data.files)exportFile(file,file.name);return;}send({type:'share',text:[data.title,data.text].filter(Boolean).join('\\n'),url:data.url||''});}});}catch(e){}
    try{if(!navigator.clipboard)Object.defineProperty(navigator,'clipboard',{value:{writeText:async function(text){send({type:'clipboard',text:String(text)});}}});}catch(e){}
  })();true;`;
}
