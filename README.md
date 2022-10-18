#Writeup WebGoat 
---
### Intro
WebGoat is a deliberately insecure application that allows interested developers just like you to test vulnerabilities commonly found in Java-based applications that use common and popular open source components.

Link : https://owasp.org/www-project-webgoat/


### Setup :
- Cần có sẵn docker.
- Tải file image về : `docker pull webgoat/goatandwolf`
- Khởi chạy container : `docker run -p 127.0.0.1:8080:8080 -p 127.0.0.1:9090:9090 -e TZ=<timezone> webgoat/webgoat`
> Chú ý: Thay thế `<timezone>` bằng giá trị timezone của bạn. Nếu bạn không biết thì hãy dùng câu lệnh : `cat /etc/timezone`
- Mở browser với URL : `localhost:8080/WebGoat` và đăng ký tài khoản

Vậy là ta đã hoàn thành.

Đọc thêm hướng dẫn chi tiết tại đây : https://github.com/WebGoat/WebGoat