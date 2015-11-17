Neoeedit is a light, quick, smart, simple text editor.
It's mainly written in Java. Size is only about 100KB(Jar File).

The target is to replace notepad.exe and to be the daily used editor for programmers.

Current status:
Stable.





Newest Version of Java(JRE) need to be installed.

# Download #

You can use it in any of the methods:
  * <img src='http://neoeworld.com/jar.jpg'></img> Download the newest <a href='http://neoeedit.googlecode.com/svn/trunk/dist/neoeedit.jar'>neoeedit.jar</a>, and run local.
  * Add to context menu of Windows (Open with neoeedit, very useful): (change some path in file if needed) <a href='http://neoeedit.googlecode.com/svn/trunk/neoeedit.reg'>neoeedit.reg</a>
  * How to RunOnLinux.
  * Windows standalone executable <a href='http://neoeedit.googlecode.com/svn/trunk/binary/neoeedit.exe'>neoeedit.exe</a>


<img src='http://neoeedit.googlecode.com/files/neoeedit1.png' alt='screenshot'></img>
<br>
<img src='http://neoeworld.com/n/apps.Download?k=neoeedit-font-setting.png.1402411152292'></img>

<hr />
features:<br>
<br>
<ul><li>basic functions of text editor<br>
</li><li>small code (about Kilo lines)<br>
</li><li>small memory footprint (about 100KB binary size)<br>
</li><li>quick show (customized Swing component)<br>
</li><li>good unicode support<br>
</li><li>run where Java run: Windows, Linux, ...<br>
</li><li>IME-Aware, on-the-spot pre-edting.</li></ul>

<ul><li>shortcut keys, fonts, colors can be configured by editing config file(user's-home-dir/.neoeedit/data.py.verX)<br>
</li><li>ctrl-C/V/X for copy/paste/cut<br>
</li><li>select text using both mouse and keyboard<br>
</li><li>cursor keys: up down left right home end(also Ctrl-Enter) pageup pagedown<br>
</li><li>alt-pageup, alt-pagedown, alt-mouse scroll: horizon cursor movement<br>
</li><li>line number</li></ul>

<ul><li>alt-Z: move cursor back by history<br>
</li><li>alt-Y: move cursor forward by history<br>
</li><li>ctrl-L: goto line<br>
</li><li>ctrl-A: select all<br>
</li><li>ctrl-D: delete current line<br>
</li><li>ctrl-R: remove all trailing space<br>
</li><li>alt-H:show hex for selected string<br>
</li><li>alt-W: wrap lines.(at current X(min 10), non-English character's width calculated as two.)</li></ul>

<ul><li>ctrl-S: save file<br>
</li><li>F2    : save as...<br>
</li><li>ctrl-O: open file in directory. It just list them, and use ctrl-G to open one of them.<br>
</li><li>drag and drop files to open<br>
</li><li>ctrl-N: new empty document in window<br>
</li><li>ctrl-M: new one More window.<br>
</li><li>ctrl-Q: show all opened documents in window. You can jump to one of them by press ctrl-1 over it.<br>
</li><li>ctrl-1: go to file(:lineno) of current line.<br>
</li><li>ctrl-Tab: quick switch between opened documents.<br>
</li><li>ctrl-W: close current document, and record to open file history.<br>
</li><li>ctrl-G or ctrl-1: goto file and line on search result or file by name or document in the window by name.<br>
</li><li>Alt-L: launch current line using system default launcher(for file, executable, text, or URL).<br>
</li><li>Alt-E: execute current line in system command line(eg. for windows, try "cmd /c dir").<br>
</li><li>ctrl-H: open file history<br>
</li><li>ctrl-P: print (beautifully)</li></ul>

<ul><li>ctrl-Z: undo<br>
</li><li>ctrl-Y: redo</li></ul>

<ul><li>ctrl-F: find/replace<br>
</li><li>F3    : find next</li></ul>

<ul><li>ctrl-E: set encoding<br>
</li><li>F5    : reload with encoding<br>
</li><li>alt-S : change line seperator between windows(\r\n) and unix(\n)</li></ul>

<ul><li>alt-left alt-right: quick indent<br>
</li><li>home end, ctrl up,down,left,right: cursor control</li></ul>

<ul><li>common language keywords highlighting (java,c,python,basic, 500+ words)</li></ul>

<ul><li>(){}<><a href='.md'>.md</a> pair marking. Alt-P: move cursor between pair marks.</li></ul>

<ul><li>F1: help(about)</li></ul>

<ul><li>Encoding auto detection . for UTF-8, UTF-16, UTF-32, SJIS, GBK. Good unicode support.<br>
</li><li>Comments auto detection . like # // -- for many languages.</li></ul>

<ul><li>replace/find in files, and in dir/sub-dir</li></ul>

<ul><li>alt-\: rectangular mode</li></ul>

<ul><li>ctrl-mouse scroll: zoom in/out<br>
</li><li>ctrl-0: zoom reset</li></ul>

<ul><li>alt-f:list fonts, then ctrl-1 on it to change font</li></ul>

<ul><li>alt-j: <a href='JsMacro.md'>javascript macro</a></li></ul>

<ul><li>alt-c: switch between preset color modes, there are 3 now: White, Black, Blue.</li></ul>

<ul><li>It's also a image viewer! View large JPG, GIF, BMP, PNG images easily.<br>
<ul><li>Left, Right: view previous/next image<br>
</li><li>Up, Down: rotate image<br>
</li><li>0: reset image<br>
</li><li>Ctrl-W / H / O: functions like what it did in text editor mode<br>
</li><li>F1 or TAB: toggle thumbnail</li></ul></li></ul>

<ul><li>configurable custom Freetype font</li></ul>

<ul><li>integrated <a href='NeoeIme.md'>NeoeIme</a> as a plugin.(see <a href='Plugins.md'>Plugins</a>)<br>
<blockquote>ctrl-space to toggle IME</blockquote></li></ul>



<ul><li>hold CTRL for a second: show command panel.<br>
</li><li>(more... todo)</li></ul>

<a href='http://code.google.com/p/neoeedit/source/list'>changelog</a>

Neoeedit is free and open source. However if you like it, you could support it by buying it.<br>
<a href='http://neoeworld.com/buy_neoeedit.html'>buy neoeedit</a>