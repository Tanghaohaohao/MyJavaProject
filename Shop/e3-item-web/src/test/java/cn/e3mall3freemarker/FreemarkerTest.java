package cn.e3mall3freemarker;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class FreemarkerTest {

	@Test
	public void testFreeMarker() throws Exception{
		// 第一步：创建一个Configuration对象，直接new一个对象。构造方法的参数就是freemarker对于的版本号。
		Configuration configuration = new Configuration(Configuration.getVersion());
		// 第二步：设置模板文件所在的路径。
		configuration.setDirectoryForTemplateLoading(new File("E:/xiaohui/MyCode/e3-item-web/src/main/webapp/WEB-INF/ftl"));
		// 第三步：设置模板文件使用的字符集。一般就是utf-8.
		configuration.setDefaultEncoding("utf-8");
		// 第四步：加载一个模板，创建一个模板对象。
		Template template = configuration.getTemplate("student.ftl");
		// 第五步：创建一个模板使用的数据集，可以是pojo也可以是map。一般是Map。
		Map dataModel = new HashMap<>();
		//向数据集中添加数据
		dataModel.put("hello", "this is my first freemarker test.");
		//创建pojo对象
		Student student = new Student(1,"小明",18,"南江");
		dataModel.put("student", student);
		List<Student> stuList = new ArrayList<Student>();
		stuList.add(new Student(1,"小明",18,"南江"));
		stuList.add(new Student(2,"小明",19,"南江"));
		stuList.add(new Student(3,"小明",20,"南江"));
		stuList.add(new Student(4,"小明",21,"南江"));
		stuList.add(new Student(5,"小明",22,"南江"));
		stuList.add(new Student(6,"小明",23,"南江"));
		stuList.add(new Student(7,"小明",24,"南江"));
		dataModel.put("stuList", stuList);
		dataModel.put("date", new Date());
		dataModel.put("val", null);
		// 第六步：创建一个Writer对象，一般创建一FileWriter对象，指定生成的文件名。
		Writer out = new FileWriter(new File("C:/Users/tangmaoqin/Desktop/student.html"));
		// 第七步：调用模板对象的process方法输出文件。
 		template.process(dataModel, out);
		// 第八步：关闭流。
		out.close();

	}
}
