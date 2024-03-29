import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Client.java
 * @author Lulamela Mfenyana
 * student number: 208097104
 */
public class Client {

    private static ObjectInputStream inStream;
    private static ObjectOutputStream outputStream;
    private static JFrame frame;

    public static void initialize() {
        connectToServer();
        setClientUI();
    }

    private static void setClientUI() {
        frame = new JFrame();
        frame.setTitle("Dvd Rental Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        mainPanel.add(customerPanel());
        mainPanel.add(dvdPanel());

        mainPanel.add(rentDvdPanel());

        mainPanel.add(movieListPanel());
        mainPanel.add(customerListPanel());
        mainPanel.add(searchMoviePanel());
        mainPanel.add(rentalListPanel());
        mainPanel.add(outstandingRentalListPanel());
        mainPanel.add(returnRentalPanel());
        mainPanel.add(searchRentalPanel());

        frame.add(new JScrollPane(mainPanel));

        frame.setSize(1000, 700);
        frame.setVisible(true);
    }

    public static void connectToServer() {
        initiateConnectionToServer();
    }

    private static void initiateConnectionToServer() {
        try {
            Socket connectToServer = new Socket("localhost", 5559);
            System.out.println("Connection Established");

            outputStream = new ObjectOutputStream(connectToServer.getOutputStream());
            inStream = new ObjectInputStream(connectToServer.getInputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static JPanel rentDvdPanel() {
        JPanel mainPanel = new JPanel();
        JPanel panel = new JPanel();
        GridLayout gridLayout = new GridLayout(0, 2);
        panel.setLayout(gridLayout);

        JButton proceedDvdBtn = new JButton("Proceed");
        JButton rentDvdBtn = new JButton("Rent DVD");

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        try {

            List<Customer> customerList;

            outputStream.writeObject("list customers");
            customerList = (ArrayList<Customer>) inStream.readObject();

            // customer info
            List<String> customers = new ArrayList<>();
            for (Customer customer : customerList) {
                customers.add(String.format("%s ----- %s ----- %s", customer.getCustNumber(), customer.getName(),
                        customer.getSurname()));
            }
            JList<String> customerJList = new JList(customers.toArray());
            JScrollPane scrollPane = new JScrollPane(customerJList);

            String[] category = {"Horror", "Sci-fi", "Drama", "Romance", "Comedy", "Action", "Cartoon"};
            JList categoryJList = new JList(category);

            JScrollPane scrollPane2 = new JScrollPane(categoryJList);

            panel.add(scrollPane);
            panel.add(scrollPane2);


            panel.add(proceedDvdBtn);

            JList dvdList = new JList();
            List<String> dvds = new ArrayList<>();
            proceedDvdBtn.addActionListener(e -> {
                try {
                    outputStream.writeObject("list movies for category");
                    outputStream.writeObject(category[categoryJList.getSelectedIndex()]);
                    List<DVD> movieList = (ArrayList<DVD>) inStream.readObject();

                    for (DVD dvd : movieList) {
                        dvds.add(String.format("%s ----- %s ----- %s ----- %s", dvd.getDvdNumber(), dvd.getTitle(),
                                dvd.getCategory(), dvd.getPrice()));
                    }

                    dvdList.setListData(dvds.toArray());

                    JScrollPane scrollPane3 = new JScrollPane(dvdList);
                    panel.add(scrollPane3);
                    panel.add(rentDvdBtn);
                    SwingUtilities.updateComponentTreeUI(frame);
                } catch (IOException | ClassNotFoundException ioException) {
                    ioException.printStackTrace();
                }
            });

            rentDvdBtn.addActionListener(e -> {
                String custNumber = new StringTokenizer(customerJList.getSelectedValue()).nextToken(" -");
                String dvdNumber = new StringTokenizer((String) dvdList.getSelectedValue()).nextToken(" -");

                try {

                    outputStream.writeObject("rent dvd");
                    outputStream.writeObject(custNumber);
                    outputStream.writeObject(dvdNumber);

                    String message = (String) inStream.readObject();

                    JOptionPane.showMessageDialog(frame, message, "Message Dialog", JOptionPane.INFORMATION_MESSAGE);

                } catch (IOException | ClassNotFoundException ioException) {
                    ioException.printStackTrace();
                }
            });

        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }


        mainPanel.add(new JLabel("Rent DVD"));
        mainPanel.add(panel);
        return mainPanel;
    }

    private static JPanel customerPanel() {
        JPanel mainPanel = new JPanel();
        JPanel panel = new JPanel();
        GridLayout gridLayout = new GridLayout(0, 2);
        panel.setLayout(gridLayout);
        panel.setSize(300, 50);

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        JTextField nameTextField = new JTextField("name", 12);
        JTextField surnameTextField = new JTextField("surname", 12);
        JTextField phoneNumTextField = new JTextField("phone number", 12);

        JButton addCustomerBtn = new JButton("Add New Customer");
        addCustomerBtn.setSize(75, 75);

        addCustomerBtn.addActionListener(e -> {
            try {
                outputStream.writeObject("add customer");
                outputStream.flush();
                Customer newCustomer = new Customer();
                newCustomer.setCustNumber(Math.abs(new SecureRandom().nextInt(9456198)));
                newCustomer.setName(nameTextField.getText());
                newCustomer.setSurname(surnameTextField.getText());
                newCustomer.setPhoneNum(phoneNumTextField.getText());
                newCustomer.setCredit(100);
                newCustomer.setCanRent(true);

                System.out.println(newCustomer);

                nameTextField.setText("name");
                surnameTextField.setText("surname");
                phoneNumTextField.setText("phone number");

                outputStream.flush();
                outputStream.writeObject(newCustomer);

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });


        mainPanel.add(new JLabel("Add Customer"));
        panel.add(nameTextField);
        panel.add(surnameTextField);
        panel.add(phoneNumTextField);
        panel.add(addCustomerBtn);
        mainPanel.add(panel);

        return mainPanel;
    }

    private static JPanel dvdPanel() {
        JPanel mainPanel = new JPanel();
        JPanel panel = new JPanel();
        GridLayout gridLayout = new GridLayout(0, 2);
        panel.setLayout(gridLayout);
        panel.setSize(400, 50);

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        JTextField titleTextField = new JTextField("title", 8);

        String[] category = {"horror", "Sci-fi", "Drama", "Romance", "Comedy", "Action", "Cartoon"};
        JList categoryList = new JList(category);

        JScrollPane categoryScrollPane = new JScrollPane(categoryList);
        categoryScrollPane.setPreferredSize(new Dimension(70, 50));

        Checkbox newReleaseCheckbox = new Checkbox("New Release");
        Checkbox availableForRentCheckbox = new Checkbox("Available for Rent");

        JButton addDvdBtn = new JButton("Add New DVD");
        addDvdBtn.setSize(75, 75);

        addDvdBtn.addActionListener(e -> {
            try {
                outputStream.writeObject("add dvd");
                outputStream.flush();
                DVD dvd = new DVD();
                dvd.setDvdNumber(Math.abs(new SecureRandom().nextInt()));
                dvd.setTitle(titleTextField.getText());
                dvd.setCategory(categoryList.getSelectedIndex());
                dvd.setRelease(newReleaseCheckbox.getState());
                dvd.setAvailable(availableForRentCheckbox.getState());

                System.out.println(dvd);

                titleTextField.setText("title");
                categoryList.setSelectedIndex(1);
                newReleaseCheckbox.setState(false);
                availableForRentCheckbox.setState(false);

                outputStream.flush();
                outputStream.writeObject(dvd);

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        mainPanel.add(new JLabel("Add DVD"));
        panel.add(titleTextField);
        panel.add(categoryScrollPane);
        panel.add(newReleaseCheckbox);
        panel.add(availableForRentCheckbox);
        panel.add(addDvdBtn);
        mainPanel.add(panel);

        return mainPanel;
    }

    private static JPanel rentalListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setSize(300, 50);
        JButton listRentalsBtn = new JButton("List All Rentals");
        listRentalsBtn.setSize(150, 150);
        listRentalsBtn.addActionListener(e -> {
            try {
                String[][] data;

                String[] columnNames = {"#", "Date Rented", "Date Returned", "Customer Number", "DVD Number", "Total Penalty Cost"};
                List<Rental> rentalList;

                outputStream.writeObject("list rentals");
                setRentalJTable(panel, columnNames);

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        panel.add(new JLabel("List All Rentals"));
        panel.add(listRentalsBtn);

        return panel;
    }

    private static JPanel returnRentalPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setSize(300, 50);
        JButton returnRentalBtn = new JButton("Return Rental");
        returnRentalBtn.setSize(150, 150);
        try {
            String[][] data;

            String[] columnNames = {"#", "Date Rented", "Date Returned", "Customer Number", "DVD Number", "Total Penalty Cost"};
            List<Rental> rentalList;

            outputStream.writeObject("list outstanding rentals");
            rentalList = (ArrayList<Rental>) inStream.readObject();

            int size = rentalList.size();
            data = new String[size][6];

            for (int i = 0; i < size; i++) {
                Rental el = rentalList.get(i);
                data[i][0] = ((Integer) el.getRentalNumber()).toString();
                data[i][1] = el.getDateRented();
                data[i][2] = el.getDateReturned();
                data[i][3] = ((Integer) el.getCustNumber()).toString();
                data[i][4] = ((Integer) el.getDvdNumber()).toString();
                data[i][5] = ((Double) el.getTotalPenaltyCost()).toString();
            }

            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(800, 100));
            SwingUtilities.updateComponentTreeUI(frame);

            returnRentalBtn.addActionListener(e -> {
                try {
                    outputStream.writeObject("return dvd");
                    outputStream.writeObject(rentalList.get(table.getSelectedRow()));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });

            panel.add(new JLabel("Return Rental"));
            panel.add(scrollPane);
            panel.add(returnRentalBtn);

        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }


        return panel;
    }

    private static JPanel outstandingRentalListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setSize(300, 50);
        JButton listOutsdRentalsBtn = new JButton("List Outstanding Rentals");
        listOutsdRentalsBtn.setSize(150, 150);
        listOutsdRentalsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String[][] data;

                    String[] columnNames = {"#", "Date Rented", "Date Returned", "Customer Number", "DVD Number", "Total Penalty Cost"};
                    List<Rental> rentalList;

                    outputStream.writeObject("list outstanding rentals");
                    setRentalJTable(panel, columnNames);

                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        });

        panel.add(new JLabel("List Outstanding Rentals"));
        panel.add(listOutsdRentalsBtn);

        return panel;
    }

    private static JPanel searchRentalPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JButton searchRentalsBtn = new JButton("Search Rentals");
        JTextField searchTextField = new JTextField("Enter date as yyyy/mm/dd");
        searchTextField.setSize(100, 50);

        searchRentalsBtn.setSize(150, 150);
        searchRentalsBtn.addActionListener(e -> {
            try {
                String[][] data;

                String[] columnNames = {"#", "Date Rented", "Date Returned", "Customer Number", "DVD Number", "Total Penalty Cost"};
                List<Rental> rentalList;

                outputStream.writeObject(searchTextField.getText());
                setRentalJTable(panel, columnNames);

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        panel.add(new JLabel("Search rentals by date"));
        panel.add(searchTextField);
        panel.add(searchRentalsBtn);

        return panel;
    }

    private static void setRentalJTable(JPanel panel, String[] columnNames) throws IOException, ClassNotFoundException {
        List<Rental> rentalList;
        String[][] data;
        rentalList = (ArrayList<Rental>) inStream.readObject();

        int size = rentalList.size();
        data = new String[size][6];

        for (int i = 0; i < size; i++) {
            Rental el = rentalList.get(i);
            data[i][0] = ((Integer) el.getRentalNumber()).toString();
            data[i][1] = el.getDateRented();
            data[i][2] = el.getDateReturned();
            data[i][3] = ((Integer) el.getCustNumber()).toString();
            data[i][4] = ((Integer) el.getDvdNumber()).toString();
            data[i][5] = ((Double) el.getTotalPenaltyCost()).toString();
        }

        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 100));
        panel.add(scrollPane);
        SwingUtilities.updateComponentTreeUI(frame);
    }

    private static JPanel searchMoviePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JTextField searchTextField = new JTextField("Enter a name for a movie");
        searchTextField.setSize(100, 50);

        panel.setSize(500, 150);

        searchTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    String[][] data;

                    String[] columnNames = {"#", "Date Rented", "Date Returned", "Customer Number", "DVD Number", "Total Penalty Cost"};
                    List<DVD> movieList;

                    outputStream.writeObject("search movies");
                    outputStream.writeObject(searchTextField.getText());
                    setMovieJTable(columnNames, panel);

                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        panel.add(new JLabel("Search Movies"));
        panel.add(searchTextField);

        return panel;
    }

    private static void setMovieJTable(String[] columnNames, JPanel panel) throws IOException, ClassNotFoundException {
        List<DVD> movieList;
        String[][] data;
        movieList = (ArrayList<DVD>) inStream.readObject();

        int size = movieList.size();
        data = new String[size][6];

        for (int i = 0; i < size; i++) {
            DVD el = movieList.get(i);
            data[i][0] = ((Integer) el.getDvdNumber()).toString();
            data[i][1] = el.getTitle();
            data[i][2] = el.getCategory();
            data[i][3] = ((Double) el.getPrice()).toString();
            data[i][4] = ((Boolean) el.isNewRelease()).toString();
            data[i][5] = ((Boolean) el.isAvailable()).toString();
        }

        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 100));
        panel.add(scrollPane);
        SwingUtilities.updateComponentTreeUI(frame);
    }

    private static JPanel movieListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setSize(300, 50);
        JButton listMoviesBtn = new JButton("List Movies");
        listMoviesBtn.setSize(150, 150);
        listMoviesBtn.addActionListener(e -> {
            try {
                String[][] data;

                String[] columnNames = {"#", "Title", "Category", "Price", "New Release", "Available for Rent"};
                List<DVD> movieList;

                outputStream.writeObject("list movies");
                setMovieJTable(columnNames, panel);

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        panel.add(new JLabel("List movies"));
        panel.add(listMoviesBtn);

        return panel;
    }

    private static JPanel customerListPanel() {
        JPanel panel = new JPanel();
        GridLayout gridLayout = new GridLayout(3, 1);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setSize(300, 50);
        JLabel label = new JLabel("List customers");
        JButton listMoviesBtn = new JButton("List Customers");
        listMoviesBtn.setSize(150, 150);
        listMoviesBtn.addActionListener(e -> {
            try {
                String[][] data;

                String[] columnNames = {"#", "Firstname", "Surname", "Phone Number", "Credit", "Can Rent"};
                List<Customer> customerList;

                outputStream.writeObject("list customers");
                customerList = (ArrayList<Customer>) inStream.readObject();

                int size = customerList.size();
                data = new String[size][6];

                for (int i = 0; i < size; i++) {
                    Customer el = customerList.get(i);
                    data[i][0] = ((Integer) el.getCustNumber()).toString();
                    data[i][1] = el.getName();
                    data[i][2] = el.getSurname();
                    data[i][3] = el.getPhoneNum();
                    data[i][4] = ((Double) el.getCredit()).toString();
                    data[i][5] = ((Boolean) el.canRent()).toString();
                }

                JTable table = new JTable(data, columnNames);
                JScrollPane scrollPane = new JScrollPane(table);
                scrollPane.setPreferredSize(new Dimension(800, 100));
                panel.add(scrollPane);
                SwingUtilities.updateComponentTreeUI(frame);

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        panel.add(label);
        panel.add(listMoviesBtn);

        return panel;
    }

}
