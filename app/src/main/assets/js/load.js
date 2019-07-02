var server=["http://getlist5.obovse.ru/jsapp/proxy/https://raw.githubusercontent.com/alexkdpu/JS/master/getsettings.js","http://getlist4.obovse.ru/jsapp/proxy/https://raw.githubusercontent.com/alexkdpu/JS/master/getsettings.js","https://raw.githubusercontent.com/alexkdpu/JS/master/getsettings.js"];
var box_client="aForkPlayer2.5";
var loader_platform="aforkplayer_apk";
var version_client="aForkPlayer3.0";
var version_local_files=190701;
var version_local_js=1;

var ttId;
(function (){
	var tCount=0;
    ttId = setInterval ( function ()  {
    	tCount++;
        if  ((document.readyState ==  "complete"&&tCount>15)||tCount>250) onComplete ();
    },  40 );
    function onComplete (){
	    clearInterval (ttId);
		loadstartinfo("Start loader v3.0");
		var s=LreadFile("Ls");
	    if(s=="") {
	    	loadstartinfo("First run! Update settings...");
	    }
	    var tt=40;
		for(var i=0;i<server.length;i++){
			console.log("tt="+tt);
			if(i==server.length-2&&s=="") var dop=12000;
			else if(s=="") dop=6000;
			else dop=2500;
			setTimeout("if(!Lprload) LUrlRequest('"+server[i]+"','getsettings',"+dop+");",tt);
			tt+=dop;
		}



    };
})();

function Laddjs(u){
	var script   = document.createElement('script');
	script.type  = 'text/javascript';
	script.async = true;
	script.src   = u;
	script.onload =function(){};
	document.getElementsByTagName('head')[0].appendChild(script);
}

function keyHandlerPress(event){
	console.log(event);
	keyHandler(event);
}
document.addEventListener("keydown",keyHandlerPress,true);

var startcmd0="";
function loadstartinfo(s){
	try{
	startcmd0+=s+"<br>";
	document.getElementById("startcmd").innerHTML=startcmd0;
	}
	catch(e){}
}
function LreadFile(n){
	var s="";
	try{
		if(loader_platform=="samsung_maple"){
				var fileSystemObj = new FileSystem();
				var fileObj = fileSystemObj.openCommonFile(curWidget.id + '/'+n,'r');
				if(fileObj!=null){
					while(1){
						line=fileObj.readLine();
						if (line==null)
						break;
						s+=line;
					}
					fileSystemObj.closeCommonFile(fileObj);
				}
				else s="";
		}
		else{
				if (window.localStorage !== null) {
					if(typeof window['localStorage'].getItem(n) !="string" || typeof localStorage.getItem(n)=="undefined") s= "";
					else s= window["localStorage"].getItem(n);
				}
				else {
					loadstartinfo("Not support read file");
				}
		}

	}
	catch(e){loadstartinfo("Error read file: "+e.message); s="";}
	return s;
}

 function LwriteFile(n,v){
	try{
		v=v.toString();
		v=""+v;
		v=v.replace(/\r/g,"").replace(/\n/g,"").replace(/\t/g,"");


		if(loader_platform=="samsung_maple"){
			v=""+v;
			var fileSystemObj = new FileSystem();
			var fileObj = fileSystemObj.openCommonFile(curWidget.id + '/'+n,'w');
			if(fileObj!=null){
				fileObj.writeAll(v);
				fileSystemObj.closeCommonFile(fileObj);
			}
		}
		else{
				if (window.localStorage !== null) {
					window["localStorage"].setItem(n,v);
				}
				else {
					loadstartinfo("Not support write file");
				}

		}
	}
	catch(e){loadstartinfo("Error write file: "+e.message);return "";}
}

var Lprload=false,Lprload2=false,Lprload3=false,LfalseCount=0,LfalseCount2=0,LfalseCount3=0,Ldata={},Ldatasrc="",Ltm=1;
function LUrlRequest(url,mode,t) {
        console.log("get url: "+url);
		var xhrt = null,orig_url=url, timer;
		if(t==null) t=12000;
		try{
		xhrt = new XMLHttpRequest();
		xhrt.onreadystatechange = function () {
			if (xhrt.readyState == 4) {
				var s=xhrt.responseText;
				if(mode=="getjs"){
					if(s.indexOf("function tomflight_fail")>0){
						var s2=s.replace(/\n/g,"_RN_");
						LwriteFile("Ljs", s2);
					}
					else{
						if(++LfalseCount3==Ldata.length){
							var s=LreadFile("Ljs").replace(/_RN_/g,"\n");
							if(s.indexOf("function tomflight_fail=")==-1) loadstartinfo("Error load js! Restart application");
						}
						else return;
					}
					if(document.getElementById("Ljs")!=null){
						console.log("del Ljs");
						try{
							var el = document.getElementById("Ljs");
							el.parentNode.removeChild(el);
						}
						catch(e){}
					}
					var script   = document.createElement('script');
					script.type  = 'text/javascript';
					script.id   = "Ljs";
					script.innerHTML=Ldatasrc+"\n"+s;
					script.onload =function(){};
					document.getElementsByTagName('head')[0].appendChild(script);

				}
				if(mode=="getdata"){
					if(s.indexOf("ipC=")>0){
						console.log("get data");
						Lprload2=true;
						LwriteFile("Ld", s);
						loadstartinfo("Data OK! ");
					}
					else{
						if(++LfalseCount2==Ldata.length){
							var s=LreadFile("Ld");
							Ltm++;
							if(s.indexOf("ipC=")==-1) loadstartinfo("Error load data! Restart application");
							else loadstartinfo("Data offline OK! Check your internet connection");
						}
						else return;
					}
					try{
						Ldatasrc=s;

					}
					catch(e){
						loadstartinfo("Error load data! "+e.message);
					}
					loadstartinfo("ForkPlayer js..");

					s=LreadFile("Ljs");
					var tt=40;
					for(var i=0;i<Ldata.length;i++){
						console.log("td="+Ldata[i][2]/Ltm+" / "+tt);
						setTimeout("if(typeof tomflight_fail == 'undefined') LUrlRequest('"+Ldata[i][1]+"','getjs',"+Ldata[i][2]/Ltm+");",tt);
						tt=tt+Ldata[i][2]/Ltm;
					}
				}
				if(mode=="getsettings"){
					Ldata=LJsonParse(s);
					if(Ldata!=null){
						console.log("get sets");
						Lprload=true;
						LwriteFile("Ls", s);
						loadstartinfo("Prepare the settings OK! ");
					}
					else{
						if(++LfalseCount==server.length){
							Ltm++;
							Ldata=LJsonParse(LreadFile("Ls"));
						    if(Ldata==null) return loadstartinfo("Error load settings! Check your internet connection");
						    else loadstartinfo("Prepare the settings in offline OK! Check your internet connection");

						}
						else return;
					}

					loadstartinfo("Load data..");
					s=LreadFile("Ld");
					var tt=40;
					for(var i=0;i<Ldata.length;i++){
						console.log("td="+tt);
						if(i==Ldata.length-2&&s=="") var dop=12000;
						else if(s=="") dop=6000;
						else if(Ltm==2) dop=1800;
						else dop=2500;
						setTimeout("if(!Lprload2) LUrlRequest('"+Ldata[i][0]+"','getdata',"+dop+");",tt);
						tt+=dop;
					}
					/*
						n=40
						for(var i=0;i<=m.length;i++){
							addjs(m[i][0],n,i);
							n+=m[i][1]; //Timeout
						}
					*/
				}
			};
		};

		xhrt.open('GET', url, true);
		xhrt.send();
		}
		catch(e){
		    console.log(e);
		}
		time=setTimeout(function(){xhrt.abort();},t);
}
function LJsonParse(s){
	try{
		if(s=="") return null;
		return JSON.parse(s);
	}
	catch(e){return null;}
}
