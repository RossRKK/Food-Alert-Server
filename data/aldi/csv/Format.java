import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.io.File;
import java.io.FileOutputStream;


public class Format {

    public static void main(String[] args) {

        String csvFile = args[0];
        BufferedReader br = null;
        boolean inQuote = false;
        char line;

        FileOutputStream fop = null;
		    File file;

        try {
          file = new File(args[1]);
          fop = new FileOutputStream(file);

          // if file doesnt exists, then create it
          if (!file.exists()) {
            file.createNewFile();
          }


            br = new BufferedReader(new FileReader(csvFile));
            int intChar = br.read();
            while (intChar != -1) {
                line = (char) intChar;
                if (line == '"') {
                    inQuote = !inQuote;
                }

                if (inQuote && line == '\n') {
                    line = ' ';
                }

                if (!inQuote && line == ',') {
                    line = ';';
                }
                if (inQuote && line != '\r')
                  fop.write(line);
                else if (!inQuote) {
                  fop.write(line);
                }
                intChar = br.read();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
