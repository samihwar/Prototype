package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Notification;
import il.cshaifasweng.OCSFMediatorExample.entities.Task;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import static il.cshaifasweng.OCSFMediatorExample.client.SimpleClient.message;
import static il.cshaifasweng.OCSFMediatorExample.client.SimpleClient.tableMessage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;


import static il.cshaifasweng.OCSFMediatorExample.client.SimpleClient.*;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class TasksController {

    @FXML
    private TableView<Task> taskTable;

    @FXML
    private TableColumn<Task, Integer> taskId;

    @FXML
    private TableColumn<Task, String> taskName;

    @FXML
    private TableColumn<Task,String> user;

    @FXML
    private TableColumn<Task, String> date;

    @FXML
    private TableColumn<Task, String> volunteer;

    @FXML // fx:id="userPic"
    private TableColumn<Task, Image> userPic; // Value injected by FXMLLoader
    private TaskSubmittedEvent event;



    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
        SimpleClient.getCurrentUser().setTaskListOpen(false);
        //unsubscribe from eventBus:
        EventBusManager.getEventBus().unregister(this);
        System.out.println("(Tasks Controller) unsubscribed from eventBus");
        Message message = new Message ("#closeTaskList",SimpleClient.getCurrentUser());
        try{
            SimpleClient.getClient().sendToServer(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final EventBus eventBus = EventBusManager.getEventBus();
    @FXML
    public void initialize() {

        eventBus.register(this);
        if(eventBus.isRegistered(this)){
            System.out.println("FXML TaskController registered to event bus");
        }

        taskId.setCellValueFactory(new PropertyValueFactory<Task, Integer>("taskId"));
        taskName.setCellValueFactory(new PropertyValueFactory<Task, String>("taskName"));
        user.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUser().getUserName()));
        date.setCellValueFactory(new PropertyValueFactory<Task, String>("date"));

       //time.setCellValueFactory(new PropertyValueFactory<Task, Integer>("time"));

        volunteer.setCellValueFactory(cellData -> {
            if (cellData.getValue().getVolunteer() != null) {
                return new SimpleStringProperty(cellData.getValue().getVolunteer().getUserName());
            } else {
                return new SimpleStringProperty("");
            }
        });

        // Define a custom cell factory for the userPic column
        userPic.setCellFactory(col -> new TableCell<Task, Image>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(Image image, boolean empty) {
                super.updateItem(image, empty);

                if (empty || image == null) {
                    setGraphic(null);
                } else {
                    imageView.setImage(image);
                    imageView.setFitWidth(40); // Set desired width
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);
                }
            }
        });

        // Set cell value factory for userPic column
        userPic.setCellValueFactory(cellData -> {
            Task task = cellData.getValue();
            if (task != null && task.getUser() != null && task.getUser().getImageFile() != null) {
                return new SimpleObjectProperty<>(new Image(task.getUser().getImageFile().toURI().toString()));
            } else {
                return null;
            }
        });

        ObservableList<Task> observableTasks = FXCollections.observableArrayList((List<Task>) tableMessage.getObject());
        taskTable.setItems(observableTasks);

        taskTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showTaskDetails(newSelection);
            }
        });
       // SimpleClient.getCurrentUser().setTaskListOpen(true);

    }

    // Define methods to handle events posted on the EventBus
    @Subscribe
    public void onTaskSubmitted(TaskSubmittedEvent event) {
        System.out.println("(TasksController) event received by "+getCurrentUser().getUserName());
        //this.event = event;
        // Handle the task submission event
        Task newTask = event.getNewTask();

            Message message = new Message("#refreshRequestTable");
            try {
                SimpleClient.getClient().sendToServer(message);
            } catch (IOException b) {
                b.printStackTrace();
            }
        // Add the new task to the observable list
        ObservableList<Task> tasks = taskTable.getItems();
        tasks.add(newTask); //I should check weather same status?
        // Refresh the taskTable to reflect the changes
        taskTable.setItems(tasks);
        taskTable.refresh();

    }


    public void showTaskDetails(Task task) {
//        if(task.getStatus().equals("done"))
//             return;


        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Task Details");
        dialog.setHeaderText("Details of task: " + task.getTaskName());

        // Create labels and fields
        Label label1 = new Label("Task ID: ");
        Label label2 = new Label("Task Name: ");
        Label label3 = new Label("Date: ");
//        Label label4 = new Label("Time: ");
        Label label5 = new Label("Status: ");
        Label label6 = new Label("User: ");
        Label label7 = new Label("Details: ");
//        Label label7 = new Label("Volunteer: ");


        TextField text1 = new TextField(String.valueOf(task.getTaskId()));
        TextField text2 = new TextField(task.getTaskName());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TextField text3 = new TextField(task.getDate().format(formatter));
//        TextField text4 = new TextField(String.valueOf(task.getTime()));
        TextField text5 = new TextField(task.getStatus());
        TextField text6 = new TextField(task.getUser().getUserName());
        TextField text7 = new TextField(task.getDetails());
        Button changeStatusButton = new Button();
        if(!task.getStatus().equals("done")) {

            if (task.getVolunteer() != null && SimpleClient.getCurrentUser().getId() == task.getVolunteer().getId()) {
                changeStatusButton.setText("Done");
            } else {
                changeStatusButton.setText("I want to do this");
            }
        }
        else {
            changeStatusButton.setVisible(false);
        }
        Button AcceptRequest = new Button("Accept Help Request");
        Button DeclineRequest = new Button("Decline Help Request");
//        TextField text7 = new TextField(task.getVolunteer().getUserName());


        text1.setEditable(false);
        text2.setEditable(false);
        text3.setEditable(false);
//        text4.setEditable(false);
        text5.setEditable(false);
        text6.setEditable(false);
        text7.setEditable(false);
//        text7.setEditable(false);


        // Create layout and add items
        GridPane grid = new GridPane();
        grid.add(label1, 1, 1);
        grid.add(text1, 2, 1);
        grid.add(label2, 1, 2);
        grid.add(text2, 2, 2);
        grid.add(label3, 1, 3);
        grid.add(text3, 2, 3);
//        grid.add(label4, 1, 4);
//        grid.add(text4, 2, 4);
        grid.add(label5, 1, 4);
        grid.add(text5, 2, 4);
        grid.add(label6, 1, 5);
        grid.add(text6, 2, 5);
        grid.add(label7, 1, 6);
        grid.add(text7, 2, 6);

        //if we're in pending lists, then we don't need the volunteer button but rather the accept/decline
        if(Objects.equals(task.getStatus(), "pending")){
           grid.add(AcceptRequest,1,7);
           grid.add(DeclineRequest,1,8);
        }
        else grid.add(changeStatusButton,1,7);
//        grid.add(label7, 1, 8);
//        grid.add(text7, 2, 8);


        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.CANCEL) {
                taskTable.getSelectionModel().clearSelection();
                dialog.close();
            }
            return null;
        });

        //in case of wanting to volunteer
        changeStatusButton.setOnAction(e -> {
            if(changeStatusButton.getText().equals("I want to do this")) {
                Message message = new Message("changeStatusToIP", task, getCurrentUser());
                try {
                    SimpleClient.getClient().sendToServer(message);
                    if (task.getStatus().equals("idle")) {
                        task.setStatus("in process");
                        task.setVolunteer(getCurrentUser());
                        LocalDateTime now = LocalDateTime.now().withNano(0);
//                    task.setVolTime(now.getHour() * 3600 + now.getMinute() * 60 + now.getSecond());
//                    String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                        task.setVolDate(now);
                    }
                    taskTable.refresh();
                    dialog.close();
                } catch (IOException b) {
                    b.printStackTrace();
                }
            }
            else if(changeStatusButton.getText().equals("Done")) {
                SimpleClient.sendNotification(SimpleClient.getCurrentUser(), task.getmanagerId(),"taskDone");
                Message message = new Message("changeStatusToDone", task, getCurrentUser());
                try {
                    SimpleClient.getClient().sendToServer(message);
                    if (task.getStatus().equals("in process")) {
                        task.setStatus("Done");
                    }
                    taskTable.refresh();
                    dialog.close();
                } catch (IOException b) {
                    b.printStackTrace();
                }
            }
        });

        //in case of manager accepting help request
        AcceptRequest.setOnAction(e -> {
            System.out.println("(TaskController) clicked on accept request");
            Message message = new Message("#managerApproved",task);
            try {
                SimpleClient.getClient().sendToServer(message);
                task.setStatus("idle");
                taskTable.refresh();
                dialog.close();
            } catch (IOException b) {
                b.printStackTrace();
            }

        });

        //in case of manager accepting help request
        DeclineRequest.setOnAction(e -> {
            System.out.println("(TaskController) clicked on decline request");
            Message message = new Message("#managerDeclined",task);
            //grid.add(text6, 1, 11);
            try {
                SimpleClient.getClient().sendToServer(message);
                task.setStatus("declined");
                taskTable.refresh();
                dialog.close();
            } catch (IOException b) {
                b.printStackTrace();
            }
//             message = new Message("#showPendingList", SimpleClient.getCurrentUser());
//            try {
//                SimpleClient.getClient().sendToServer(message);
//                System.out.println("(Primary) Sending req message to server from helpReequest.");
//            } catch (IOException c) {
//                System.out.println("Failed to connect to the server.");
//                c.printStackTrace();
//            }
        });

//        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
//        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
//
//        dialog.setResultConverter(dialogButton -> {
//            if (dialogButton == saveButtonType) {
//                task.setTaskId(Integer.parseInt(text1.getText()));
//                task.setTaskName(text2.getText());
//                task.setDate(text3.getText());
//                task.setTime(Integer.parseInt(text4.getText()));
//
//                // You might need to convert the text5.getText() to a User object
//                // task.setVolunteer(...);
//                // task.setStatus(text6.getText());
//
//                // Create a new Message object with the updated task
//                Message message = new Message("#updateTask", task);
//
//                // Send the message to the server
//                try {
//                    SimpleClient.getClient().sendToServer(message);
//                    taskTable.refresh();
//                    return "Task updated";
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//            return null;
//        });

        // Show dialog
        dialog.show();

    }

    public void handlePressingSOS(ActionEvent event) {
        SimpleClient.pressingSOS("Tasks");
    }
}