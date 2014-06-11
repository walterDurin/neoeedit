package neoe.ime;

public interface Ime {
    /**a new find*/
    void find(String py);
    /**lookup info*/
    String out();    
    void prev();    
    void next();    
    String select(int index);
    String getCurrentPy();
    int getCount();
}
