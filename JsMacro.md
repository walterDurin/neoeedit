# Introduction #

JavaScript Macro in neoeedit


# Details #

use javascript engine in jdk 6.0+.


for example:
```
var i=0; //globe variables
function run(s,current,total){//lineString,currentLineNo(from 0),totalLineCount
// this method will be called per line
//example
if (current==1) {return;}// 1 line to 0 line
if (current==0) {return ['vvvvv','fasdfa',233];}// 1 line to multi line
return current+'/'+total+':'+s;// just return something
}
```

So you can use complicated javascript macro on original text whatever you can image.