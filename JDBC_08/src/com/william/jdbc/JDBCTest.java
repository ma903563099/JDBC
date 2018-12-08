package com.william.jdbc;


import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


import org.junit.Test;

import com.mysql.jdbc.PreparedStatement;

public class JDBCTest {
//	@Test
	public void testResultSetMetaData() {
		Connection connection=null;
		PreparedStatement preparedStatement=null;
		ResultSet resultSet=null;
		
		try {
			String sql="SELECT flow_id flowId,type,id_card idCard,"+
						"exam_card examCard,student_name studentName,"+
						"location,grade "+"FROM examstudent WHERE flow_id =?";
			
			connection=JDBCTools.getConnection();
			preparedStatement=(PreparedStatement) connection.prepareStatement(sql);
			
			preparedStatement.setInt(1, 10);
			resultSet=preparedStatement.executeQuery();
			
			Map<String, Object> values=new HashMap<String,Object>();
			
			
			// 1. 得到 ResultSetMetaData 对象
			ResultSetMetaData rData=resultSet.getMetaData();
			
			if (resultSet.next()) {
				// 2. 打印每一列的列名
				for (int i = 0; i < rData.getColumnCount(); i++) {
					String columnLabel=rData.getColumnLabel(i+1);
					Object columnValue = resultSet.getObject(columnLabel);
					
					values.put(columnLabel, columnValue);
				}
			}
//			System.out.println(values);
			
			Class clazz=Student.class;
			
			Object object=clazz.newInstance();
			for (Map.Entry<String, Object> entry:values.entrySet()) {
				String fieldName=entry.getKey();
				Object feildValue=entry.getValue();
				
//				System.out.println(fieldName+":"+feildValue);
				
				ReflectionUtils.setFieldValue(object, fieldName, feildValue);
			}
			
			System.out.println(object);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			JDBCTools.releaseDB(resultSet, preparedStatement, connection);
		}
	}
	@Test
	public void testGet() {
		String sql = "SELECT id,name,email,birth"+
					" FROM customers WHERE id= ?";
		
//		Customer customer=get(Customer.class,sql, 5);
//		System.out.println(customer);
		
		sql="SELECT flow_id flowId,type,id_card idCard,"+
				"exam_card examCard,student_name studentName,"+
				"location,grade "+"FROM examstudent WHERE flow_id =?";
		Student student=get(Student.class,sql,1);
		System.out.println(student);
		
	}
	
	public <T> T get(Class<T> clazz ,String sql,Object ... args) {
		T entity= null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			// 1. 得到 ResultSet 对象
			connection = JDBCTools.getConnection();
			preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
			
			for (int i = 0; i < args.length; i++) {
				preparedStatement.setObject(i+1, args[i]);
			}
			
			resultSet = preparedStatement.executeQuery();
			//2. 得到 ResultSetGetMetaData 对象
			ResultSetMetaData rData=resultSet.getMetaData();
			
			//3. 创建一个 Map<String,Object> 对象
			// 键： SQL 查询的列的别名，值： 列的值
			Map<String, Object> values=new HashMap<>();
			
			//4. 处理结果值，利用 ResultSetMetaData 填充3 对应的 Map 对象
			if (resultSet.next()) {
				for (int i = 0; i < rData.getColumnCount(); i++) {
					String columnLabel=rData.getColumnLabel(1+1);
					Object columnValue=resultSet.getObject(i+1);
					values.put(columnLabel, columnValue);
				}
				
			}
			
			//5. 若Map 不为空 ，利用反射创建 clazz 对应的对象，
			if (values.size()>0) {
				entity=clazz.newInstance();
				//5. 遍历 Map 对象，利用反射为 Class 对象的属性赋值
				for (Map.Entry<String, Object> entry:values.entrySet()) {
					String fieldName=entry.getKey();
					Object value=entry.getValue();
					
					ReflectionUtils.setFieldValue(entity, fieldName, value);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCTools.releaseDB(resultSet, preparedStatement, connection);
		}
		return entity;
		
	}
	
	public Customer getCustomer(String sql, Object... args) {
		Customer customer = null;

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = JDBCTools.getConnection();
			preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
			
			for (int i = 0; i < args.length; i++) {
				preparedStatement.setObject(i+1, args[i]);
			}
			
			resultSet = preparedStatement.executeQuery(sql);

			if (resultSet.next()) {
				customer = new Customer();
				customer.setId(resultSet.getInt(1));
				customer.setName(resultSet.getString(2));
				customer.setEmail(resultSet.getString(3));
				customer.setBirth(resultSet.getString(4));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCTools.releaseDB(resultSet, preparedStatement, connection);
		}
		return customer;

	}

	public Student getStudent(String sql, Object... args) {
		Student student = null;

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = JDBCTools.getConnection();
			preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
			
			for (int i = 0; i < args.length; i++) {
				preparedStatement.setObject(i+1, args[i]);
			}
			
			resultSet = preparedStatement.executeQuery(sql);

			if (resultSet.next()) {
				student = new Student();
				student.setFlowId(resultSet.getInt(1));
				student.setType(resultSet.getInt(2));
				student.setIdCard(resultSet.getString(3));
				student.setExamCard(resultSet.getString(4));
				student.setStudentName(resultSet.getString(5));
				student.setLocation(resultSet.getString(6));
				student.setGrade(resultSet.getInt(7));
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCTools.releaseDB(resultSet, preparedStatement, connection);
		}
		return student;

	}

	/**
	 * 使用 preparedStatement 将有效的解决 SQL 注入问题
	 */
//	@Test
	public void testSQLInjection2() {
		String username = "a' OR password=";
		String password = " OR '1'='1";

		// SELECT * FROM users WHERE username='a' OR password=' AND password =' OR
		// '1'='1'

		String sql = "SELECT * FROM users WHERE username=? AND  password=? ";

		System.out.println(sql);

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			connection = JDBCTools.getConnection();
			preparedStatement = (PreparedStatement) connection.prepareStatement(sql);

			preparedStatement.setString(1, username);
			preparedStatement.setString(2, password);

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				System.out.println("登陆成功");
			} else {
				System.out.println("用户名和密码不匹配或用户名不存在.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCTools.releaseDB(resultSet, preparedStatement, connection);
		}

	}

	/**
	 * SQL 注入
	 */

	// @Test
	public void testSQLInjection() {
		String username = "a' OR password=";
		String password = " OR '1'='1";

		// SELECT * FROM users WHERE username='a' OR password=' AND password =' OR
		// '1'='1'

		String sql = "SELECT * FROM users WHERE username='" + username + "' AND " + "password='" + password + "'";
		System.out.println(sql);

		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;

		try {
			connection = JDBCTools.getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);

			if (resultSet.next()) {
				System.out.println("登陆成功");
			} else {
				System.out.println("用户名和密码不匹配或用户名不存在.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCTools.releaseDB(resultSet, statement, connection);
		}

	}

	// @Test
	public void testPreparedStatement() {

		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = JDBCTools.getConnection();
			String sql = "INSERT INTO customers (name,email,birth) VALUES (?,?,?)";

			preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
			preparedStatement.setString(1, "ATGUIGU");
			preparedStatement.setString(2, "william@qq.com");
			// 最外层 new Date 为java.sql.Date类型，参数为：new java.util.Date().getTime() 返回毫秒数
			preparedStatement.setDate(3, new Date(new java.util.Date().getTime()));

			preparedStatement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// preparedStatement 是 Statement 的子接口，所以可以传入
			JDBCTools.releaseDB(null, preparedStatement, connection);
		}
	}

	// @Test
	public void testGetStudent() {
		// 1. 得到查询的类型
		int searchType = getSearchTypeFromConsole();

		// 2. 具体查询学生信息
		Student student = searchStudent(searchType);

		// 3. 打印学生信息
		printStudent(student);

	}

	/**
	 * 从控制台读入一个整数，确定要查询的类型
	 * 
	 * @param student
	 *            1. 用身份证查询。 2. 用准考证查询，其他的无效， 并提示用户重新输入。
	 */
	private void printStudent(Student student) {
		if (student != null) {
			System.out.println(student);
		} else {
			System.out.println("查无此人！");
		}
	}

	/**
	 * 具体查询学生信息的方法。返回一个Student 对象，若不存在，则返回 NULL
	 * 
	 * @param searchType：1
	 *            或 2
	 * @return
	 */
	private Student searchStudent(int searchType) {

		String sql = "SELECT flowid,type,idcard,examcard," + "studentname,location,grade " + "FROM examstudent "
				+ "WHERE ";

		Scanner scanner = new Scanner(System.in);

		// 1. 根据输入的 searchType ，提示用户输入信息
		// 1.1 若 searchType 为 1 ，提示：请输入身份证号。若为2，提示：请输入准考证号
		// 2. 根据 searchType 确定 SQL
		if (searchType == 1) {
			System.out.println("请输入身份证号：");
			String idcard = scanner.next();
			sql = sql + "idcard = '" + idcard + "'";
		} else {
			System.out.println("请输入准考证号：");
			String examCard = scanner.next();
			sql = sql + "examCard = '" + examCard + "'";
		}

		// 3. 执行查询
		Student student = getStudent(sql);
		// 4. 若存在查询结果，把查询结果封装成为一个 Student 对象
		return student;
	}

	/**
	 * 根据传入的sql 返回Student 对象
	 * 
	 * @param sql
	 * @return
	 */
	private Student getStudent(String sql) {
		Student student = null;

		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;

		try {
			connection = JDBCTools.getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);

			if (resultSet.next()) {
				student = new Student(resultSet.getInt(1), resultSet.getInt(2), resultSet.getString(3),
						resultSet.getString(4), resultSet.getString(5), resultSet.getString(6), resultSet.getInt(7));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCTools.releaseDB(resultSet, statement, connection);
		}
		return student;

	}

	/**
	 * 从控制台读入一个整数，确定要查询的类型
	 * 
	 * @return 1：用身份证查询 2. 用准考证查询, 其他的无效，并提示用户重新输入
	 */
	private int getSearchTypeFromConsole() {

		System.out.println("请输入查询类型：1.用身份证查询  2. 用准考证查询");
		Scanner scanner = new Scanner(System.in);

		int type = scanner.nextInt();

		if (type != 1 && type != 2) {
			System.out.println("输入有误！ 请重新输入!");
			throw new RuntimeException();
		}

		return type;
	}

	// @Test
	public void testAddNewStudent() {
		Student student = getStudentFromConsole();
		addNewStudent2(student);
	}

	/**
	 * 从控制台输入学生信息
	 */
	private Student getStudentFromConsole() {

		Scanner scanner = new Scanner(System.in);
		Student student = new Student();

		System.out.print("FlowId：");
		student.setFlowId(scanner.nextInt());

		System.out.print("Type：");
		student.setType(scanner.nextInt());

		System.out.print("IdCard：");
		student.setIdCard(scanner.next());

		System.out.print("ExamCard：");
		student.setExamCard(scanner.next());

		System.out.print("StudentName：");
		student.setStudentName(scanner.next());

		System.out.print("Location：");
		student.setLocation(scanner.next());

		System.out.print("Grade：");
		student.setGrade(scanner.nextInt());

		return student;
	}

	public void addNewStudent2(Student student) {
		String sql = "INSERT INTO examstudent(flowid,type,idcard," + "examcard, studentname,location,grade)"
				+ " Values(?,?,?,?,?,?,?)";
		JDBCTools.update(sql, student.getFlowId(), student.getType(), student.getIdCard(), student.getExamCard(),
				student.getStudentName(), student.getLocation(), student.getGrade());

	}

	public void addNewStudent(Student student) {
		// 1.准备一条SQL语句：
		String sql = "INSERT INTO examstudent " + "VALUES(" + student.getFlowId() + "," + student.getType() + ",'"
				+ student.getIdCard() + "','" + student.getExamCard() + "','" + student.getStudentName() + "','"
				+ student.getLocation() + "'," + student.getGrade() + ")";

		System.out.println(sql);
		// 2.调用 JDBCTools 类的 update(sql) 方法执行插入操作
		JDBCTools.update(sql);
	}

}
