package nii.alloe.runs;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import nii.alloe.corpus.TermList;
import nii.alloe.tools.strings.Strings;

/**
 * @author John McCrae, National Institute of Informatics
 */
public class MakeTermsFile {

    
    public static void main(String[] args) {
        if(args.length != 2) {
            System.err.println("Two arguments input and output");
            System.exit(-1);
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            TermList termList = new TermList();
            String s;
            while((s = br.readLine()) != null) {
                if(s.matches("\\s*"))
                    continue;
                termList.add(Strings.chomp(s));
            }
            
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(args[1]));
            oos.writeObject(termList);
            oos.close();
        } catch(Exception x) {
            x.printStackTrace();
        }
    }
}
