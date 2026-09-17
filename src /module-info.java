module final_homerun {
	requires javafx.controls;
	requires java.sql;
	requires javafx.fxml;
	requires mysql.connector.java;
	
	opens application to javafx.graphics, javafx.fxml;
}
