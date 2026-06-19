# 登录注册功能使用说明

## 项目架构
本项目遵循标准的 Spring MVC 架构，分为以下层次：

### 1. Entity 层（实体层）
- **位置**: `src/main/java/shop/entity/User.java`
- **功能**: 定义用户实体类，映射数据库表
- **字段**:
  - id: 主键，自增
  - username: 用户名（3-20个字符，唯一）
  - password: 密码（至少6个字符）
  - email: 邮箱（可选，唯一）
  - createTime: 创建时间
  - updateTime: 更新时间

### 2. DAO 层（数据访问层）
- **位置**: `src/main/java/shop/dao/UserRepository.java`
- **功能**: 继承 JpaRepository，提供数据库操作
- **方法**:
  - findByUsername: 根据用户名查询
  - existsByUsername: 检查用户名是否存在
  - existsByEmail: 检查邮箱是否存在

### 3. Service 层（业务逻辑层）
- **位置**: `src/main/java/shop/service/UserService.java`
- **功能**: 处理业务逻辑
- **方法**:
  - registerUser: 用户注册（检查重复、创建用户）
  - loginUser: 用户登录（验证用户名密码）
  - findByUsername: 根据用户名查找用户
  - existsByUsername: 检查用户名是否存在

### 4. Controller 层（控制器层）
- **位置**: `src/main/java/shop/controller/AuthController.java`
- **功能**: 处理HTTP请求，路由控制
- **接口**:
  - GET /login: 显示登录页面
  - POST /login: 处理登录请求
  - GET /register: 显示注册页面
  - POST /register: 处理注册请求
  - GET /home: 用户首页（需登录）
  - GET /logout: 退出登录
  - GET /: 重定向到登录页

### 5. View 层（视图层）
- **位置**: `src/main/resources/templates/`
- **页面**:
  - login.html: 登录页面
  - register.html: 注册页面
  - home.html: 用户首页

## 环境要求

1. **JDK**: Java 21
2. **Maven**: 3.6+
3. **MySQL**: 5.7+ 或 8.0+

## 快速开始

### 1. 创建数据库

执行 `database.sql` 脚本创建数据库：

```bash
mysql -u root -p < database.sql
```

或者手动创建：

```sql
CREATE DATABASE IF NOT EXISTS shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 配置数据库连接

编辑 `src/main/resources/application.yaml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shop?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true
    username: root      # 修改为你的数据库用户名
    password: root      # 修改为你的数据库密码
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 3. 编译项目

```bash
mvn clean compile
```

### 4. 运行项目

方式一：使用 Maven
```bash
mvn spring-boot:run
```

方式二：打包后运行
```bash
mvn clean package -DskipTests
java -jar target/shop-0.0.1-SNAPSHOT.jar
```

### 5. 访问应用

打开浏览器访问：http://localhost:8080

- 默认会重定向到登录页面：http://localhost:8080/login
- 注册页面：http://localhost:8080/register

## 功能说明

### 注册功能
1. 访问 http://localhost:8080/register
2. 填写用户名（3-20个字符）
3. 填写密码（至少6个字符）
4. 确认密码
5. 选填邮箱
6. 点击注册按钮
7. 注册成功后跳转到登录页

### 登录功能
1. 访问 http://localhost:8080/login
2. 输入用户名和密码
3. 点击登录按钮
4. 登录成功后跳转到首页

### 退出登录
1. 在首页点击"退出登录"按钮
2. 清除会话并重定向到登录页

## 特性

✅ 基于 Spring MVC 标准架构
✅ JPA 自动建表（ddl-auto: update）
✅ 表单验证（用户名长度、密码长度、邮箱格式）
✅ 重复检测（用户名、邮箱唯一性）
✅ Session 管理（登录状态保持）
✅ 美观的响应式 UI 设计
✅ 错误提示和成功反馈
✅ 自动时间戳（创建时间、更新时间）

## 安全建议（生产环境）

⚠️ 当前为演示版本，生产环境需要：

1. **密码加密**: 使用 BCrypt 加密密码
   ```java
   // 注册时
   String encodedPassword = passwordEncoder.encode(password);
   
   // 登录时
   boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
   ```

2. **HTTPS**: 启用 HTTPS 协议

3. **CSRF 保护**: 启用 CSRF Token

4. **SQL 注入防护**: 当前使用 JPA 已自动防护

5. **XSS 防护**: 对输出进行转义

6. **会话管理**: 设置会话超时时间
   ```yaml
   server:
     servlet:
       session:
         timeout: 30m
   ```

## 项目结构

```
shop/
├── src/main/java/shop/
│   ├── ShopApplication.java          # 启动类
│   ├── config/
│   │   └── WebMvcConfig.java         # MVC配置
│   ├── controller/
│   │   └── AuthController.java       # 认证控制器
│   ├── dao/
│   │   └── UserRepository.java       # 用户数据访问
│   ├── entity/
│   │   └── User.java                 # 用户实体
│   └── service/
│       └── UserService.java          # 用户服务
├── src/main/resources/
│   ├── application.yaml              # 配置文件
│   └── templates/
│       ├── login.html                # 登录页
│       ├── register.html             # 注册页
│       └── home.html                 # 首页
└── database.sql                      # 数据库脚本
```

## 常见问题

### 1. 数据库连接失败
- 检查 MySQL 服务是否启动
- 确认数据库用户名密码正确
- 确认数据库 `shop` 已创建

### 2. 端口被占用
修改 `application.yaml`：
```yaml
server:
  port: 8081  # 修改为其他端口
```

### 3. 表不存在
JPA 会自动创建表，如果需要手动创建，参考 `database.sql`

## 技术栈

- Spring Boot 4.1.0
- Spring Data JPA
- Thymeleaf
- MySQL
- Lombok
- Hibernate Validator
