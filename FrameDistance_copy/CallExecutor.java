import java.io.IOException;
import java.io.*;

public class CallExecutor{
  public static void main(String[] args){

    final String filename = "test_9_1-10.csv";

    try{
      FileWriter fiw = new FileWriter(new File(filename));
      fiw.write("");
      fiw.close();
    }catch(IOException e){
      e.printStackTrace();
    }

    String result;
    for(int i = 0; i < 100; i++){
      try{
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec("java Executor");
        InputStream is = p.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        while((result = br.readLine()) != null){
          System.out.println(result);
          FileWriter fw = new FileWriter(new File(filename), true);
          fw.write(result + "\n");
          fw.close();
        }

      }catch(IOException e){
        e.printStackTrace();
      }
    }
  }
}
