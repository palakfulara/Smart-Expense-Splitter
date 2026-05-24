import javafx.application.Application;
import javafx.stage.Stage;
import util.DatabaseHelper;
import util.HomeScreen;

public class MainApp extends Application {

    private DatabaseHelper db;

    @Override
    public void start(Stage stage) {
        db = new DatabaseHelper();
        new HomeScreen(stage, db).show();
    }

    @Override
    public void stop() {
        if (db != null) db.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}