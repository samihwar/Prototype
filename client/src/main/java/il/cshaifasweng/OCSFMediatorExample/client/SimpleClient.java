package il.cshaifasweng.OCSFMediatorExample.client;

import java.io.IOException;
import java.util.List;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.scene.control.ButtonType;
import org.greenrobot.eventbus.EventBus;

//for custom alert:
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

import antlr.debug.MessageEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.Task;
import il.cshaifasweng.OCSFMediatorExample.entities.User;
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;

public class SimpleClient extends AbstractClient {

	private static SimpleClient client = null;
	public static Message message;
	public static Message tableMessage;

	private static User currentUser = null; // this is for the current user(logged in user) holds his details

	private SimpleClient(String host, int port) {
		super(host, port);
	}

	@Override
	protected void handleMessageFromServer(Object msg) {
		message = (Message) msg;

		if (msg.getClass().equals(Warning.class)) {
			EventBus.getDefault().post(new WarningEvent((Warning) msg));
		} else if (message.getMessage().equals("#showUsersList")) {
			tableMessage = message;
			try {
				App.setRoot("secondary"); // calling the fxml function will generate the initliaze of
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (message.getMessage().equals("#showMembersList")) {
			tableMessage = message;
			try {
				App.setRoot("secondary"); // calling the fxml function will generate the initliaze of
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (message.getMessage().equals("#showTasksList")) {
			tableMessage = message;
			try {
				System.out.println("(Client) Tasks list received from server");
				tableMessage = message;
				App.setRoot("Tasks"); // calling the fxml function will generate the initliaze of

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (message.getMessage().equals("#showPendingList")) {
			try {
				System.out.println("(Client) Tasks list received from server pendingg");
				tableMessage = message;
				App.setRoot("Tasks"); // calling the fxml function will generate the initliaze of
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (message.getMessage().equals("#showDoneList")) {
			try {
				System.out.println("(Client) Tasks list received from server doneee");
				tableMessage = message;
				App.setRoot("Tasks"); // calling the fxml function will generate the initliaze of
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (message.getMessage().equals("#showMyTasksList")) {
			try {
				System.out.println("(Client) User Tasks list received from server ");
				tableMessage = message;
				App.setRoot("Tasks"); // calling the fxml function will generate the initliaze of
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (message.getMessage().equals("#updateTask")) {
			System.out.println("Update request sent to server. Good job!");

		} else if (message.getMessage().equals("changeStatusToIP")) {
			if ("Done".equals(message.getObject())) {
				Platform.runLater(() -> {
					try {
						App.setRoot("Tasks"); // Navigate back to the current page
						showAlert("Successful", "You can now proceed by start doing the task," +
								" when the task is done please change the status.", Alert.AlertType.INFORMATION);
					} catch (IOException e) {
						e.printStackTrace();
					}

				});
			} else {
				Platform.runLater(() -> {
					try {
						App.setRoot("Tasks"); // Navigate back to the current page
						showAlert("Error", "Cannot choose this task please choose another.", Alert.AlertType.ERROR);
					} catch (IOException e) {
						e.printStackTrace();
					}

				});
			}
			// to update the table again
			Message message = new Message("#showTasksList", SimpleClient.getCurrentUser());
			try {
				SimpleClient.getClient().sendToServer(message);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (message.getMessage().equals("TheStatusChanged")) {
			// to update the table again
			Message message = new Message("#showMyTasksList", SimpleClient.getCurrentUser());
			try {
				SimpleClient.getClient().sendToServer(message);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (message.getMessage().equals("#taskSubmitted")) {
			Platform.runLater(() -> {
				showAlert("Task was submitted", "Now we're waiting for your manager's approval!",
						Alert.AlertType.INFORMATION);
				Task task = (Task) message.getObject();
				User manager = (User) message.getSecondObject();
				if (manager != null) {
					// sending a notification to manager
					String notification = ("You have a new task request: " + " taskname= " + task.getTaskName()
							+ " taskid= " + task.getTaskId() +
							" taskstatus= " + task.getStatus() + " taskdetails= " + task.getDetails());
					SimpleClient.sendNotification(SimpleClient.currentUser, manager.getId(), notification);
				}
			});
		}

		else if (message.getMessage().equals("#managerApproved")) {
			Platform.runLater(() -> {
				showAlert("Approved!", "The request has been approved :)", Alert.AlertType.INFORMATION);
				Task task = (Task) message.getObject();
				Message message = new Message("#showPendingList", SimpleClient.getCurrentUser());
				try {
					SimpleClient.getClient().sendToServer(message);
					System.out.println("(Primary) Sending req message to server from helpReequest.");
				} catch (IOException e) {
					System.out.println("Failed to connect to the server.");
					e.printStackTrace();
				}

				// sending a notification to everyone
				String txt = "A new help-request was opened! Come on, help us help them! TaskId=" + task.getTaskId();
				SimpleClient.sendNotification(SimpleClient.currentUser, -1, txt);
			});
		}

		else if (message.getMessage().equals("#managerDeclined")) {
			Platform.runLater(() -> {
				System.out.println("(Simple Client) manager declined");
				// Create a TextInputDialog instead of CustomAlert
				TextInputDialog textInputDialog = new TextInputDialog();
				textInputDialog.setTitle("Explanation");
				textInputDialog
						.setHeaderText("Please fill in an explanation to send to the user who opened this task.");

				// Show the dialog and wait for user input
				Optional<String> result = textInputDialog.showAndWait();

				// Check if user input is present
				if (result.isPresent()) {
					// Process user input if available
					String enteredText = result.get();
					System.out.println(message.getMessage() + message.getObject());
					Task task = (Task) message.getObject();
					SimpleClient.sendNotification(SimpleClient.currentUser, task.getUser().getId(), enteredText);
					System.out.println("(Simple Client) Sent a notification to user: " + enteredText);
				} else {
					// Handle cancel action or dialog closure
					System.out.println("(Simple Client) User canceled or closed the dialog");
					message.setMessage("#cancelDecline");
					try {
						getClient().sendToServer(message);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});


		}

		else if (message.getMessage().equals("#openTask")) {
			try {
				App.setRoot("TaskForm");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (message.getMessage().equals("#showSOSResponse")) {
			System.out.println(message.getObject());
			SOSReportsController.updateHistogramFromMessage(message);
		} else if (message.getMessage().equals("#loginSuccess")) {
			try {
				currentUser = (User) message.getObject();
				System.err.println("Login success. Welcome, " + currentUser.getUserName() + " "
						+ currentUser.getPassword() + " " + currentUser.getAge() + " " + currentUser.getGender() + " "
						+ currentUser.getCommunity() + currentUser.getCommunityManager());
				App.setRoot("primary");

			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (message.getMessage().equals("#loginFailed")) {
			try {
				Platform.runLater(() -> {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Login Failed");
					alert.setHeaderText("The username and password you entered are incorrect.");
					alert.setContentText("Please check your credentials and try again.");

					alert.showAndWait();
				});
			} catch (Exception e) {
			}
		} else if (message.getMessage().equals("#User Already Signed In!")) {
			try {
				Platform.runLater(() -> {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Login Failed");
					alert.setHeaderText("User Already Signed In!");
					alert.showAndWait();
				});
			} catch (Exception e) {
			}
		}else if (message.getMessage().equals("#LoggedOut")) {
			Platform.runLater(() -> {
				try {
					App.setRoot("Login"); // Navigate back to the login screen
					showAlert("Logout Successful", "You have been successfully logged out.",
							Alert.AlertType.INFORMATION);
				} catch (IOException e) {
					e.printStackTrace();
					showAlert("Error", "Failed to load the login page.", Alert.AlertType.ERROR);
				}
			});
		}

		else if (message.getMessage().equals("#userCreated")) {
			try {
				Platform.runLater(() -> {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("User Created");
					alert.setHeaderText("Done");
					alert.setContentText("User created successfully.");

					alert.showAndWait();
					try {
						App.setRoot("Login");
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			} catch (Exception e) {
			}
		} else if (message.getMessage().equals("#submitTask")) {
			try {
				Platform.runLater(() -> {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Task Submitted");
					alert.setHeaderText("Done");
					alert.setContentText("Task submitted successfully.");

					alert.showAndWait();
					try {
						App.setRoot("primary");
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			} catch (Exception e) {
			}
		} else if (message.getMessage().equals("#addSOSDone")) {
			Platform.runLater(() -> {
				try {
					String currentFXMLPage = (String) message.getObject(); // Implement this method to get the current
																			// FXML page
					App.setRoot(currentFXMLPage); // Navigate back to the current page
					showAlert("your request have received", "Help on the way!", Alert.AlertType.INFORMATION);
				} catch (IOException e) {
					e.printStackTrace();
					showAlert("Error", "Failed to contact help.", Alert.AlertType.ERROR);
				}
			});
		} else if (message.getMessage().equals("#showNotificationsList")) {
			System.out.println(message.getMessage() + "haayyhee");
			tableMessage = message;
			try {
				System.out.println("(Client) Notification list received from server.");
				App.setRoot("Notifications"); // calling the fxml function will generate the initliaze of

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	// example how to use it
	// receiver_id = -1 if its for all
	// SimpleClient.sendNotification(SimpleClient.getCurrentUser(),receiver_id,"message");
	public static void sendNotification(User sender, int receiverId, String notification) {
		Notification sendNot = new Notification(sender, null, notification);
		Message message = new Message("#addNotification", receiverId, sendNot);
		try {
			SimpleClient.getClient().sendToServer(message);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static User getCurrentUser() { // retrieve the current user
		return currentUser;
	}

	public static SimpleClient getClient() {
		if (client == null) {
			client = new SimpleClient("localhost", 3000);

		}
		return client;
	}

	private void showAlert(String title, String content, Alert.AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}

	// here we use it to when we press on the SOS button
	protected static void pressingSOS(String page) {
		SOS newSOS = new SOS();
		if (!page.equals("Login") && !page.equals("UserCreationForm")) {
			newSOS.setUser(getCurrentUser());
		}
		String sendingMassage = "#SOSAdd" + page;
		Message message = new Message(sendingMassage, newSOS);
		try {
			getClient().sendToServer(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
