package neoe.ime;

public class ImeUnit {

    public ImeUnit(String txt, int pylen) {
        this.txt = txt;
        this.pylen = pylen;
    }

    /**word text*/
    public String txt;

    /**pinyin length*/
    public int pylen;
    public String toString(){
        return txt+":"+pylen;
    }
}
