package webmaster;

import java.util.Arrays;
import java.util.Collection.*;
import java.util.List;

public class SearchengineTest {
    public static void main(String[] args){
        SearchEngine search = new SearchEngine();

        for (String str : args){
            System.out.println(search.search(str));
        }
    }
}
