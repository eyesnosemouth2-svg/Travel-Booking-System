import java.awt.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// ---------------- ROOM CLASS ----------------
class Room {
    private final int id;
    private final String category;
    private final double pricePerNight;
    private final String description;

    public Room(int id, String category, double pricePerNight, String description) {
        this.id = id;
        this.category = category;
        this.pricePerNight = pricePerNight;
        this.description = description;
    }

    public int getId() { return id; }
    public String getCategory() { return category; }
    public double getPricePerNight() { return pricePerNight; }

    @Override
    public String toString() {
        return "Room " + id + " | " + category + " | ₹" + pricePerNight + "/night | " + description;
    }
}

// ---------------- BOOKING CLASS ----------------
class Booking {
    private static int counter = 1;
    private final int bookingId;
    private final Room room;
    private final String guestName;
    public final LocalDate checkIn;
    public final LocalDate checkOut;
    private boolean paid;
    private final double amount;

    public Booking(Room room, String guestName, LocalDate checkIn, LocalDate checkOut, double amount) {
        this.bookingId = counter++;
        this.room = room;
        this.guestName = guestName;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.amount = amount;
        this.paid = false;
    }

    public int getBookingId() { return bookingId; }
    public void setPaid(boolean paid) { this.paid = paid; }
    public Room getRoom() { return room; }

    public String getDetails() {
        return "Booking ID: " + bookingId +
               " | Guest: " + guestName +
               " | Room: " + room.getId() +
               " | Category: " + room.getCategory() +
               " | Check-in: " + checkIn +
               " | Check-out: " + checkOut +
               " | Paid: " + paid +
               " | Amount: ₹" + amount;
    }
}

// ---------------- MAIN GUI ----------------
public class HotelReservationGUI extends JFrame {
    private final java.util.List<Room> rooms = new java.util.ArrayList<>();
    private final java.util.List<Booking> bookings = new java.util.ArrayList<>();

    private final JTextArea searchResults = new JTextArea();
    private final JTextArea viewBookingsArea = new JTextArea();
    private final JTextArea cancelBookingsArea = new JTextArea();

    public HotelReservationGUI() {
        setTitle("🏨 Deluxe Hotel Reservation System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadRooms();

        // Header panel
        JPanel header = new JPanel();
        header.setBackground(new Color(30,144,255));
        JLabel title = new JLabel("🏨 Deluxe Hotel Booking System");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        header.add(title);
        header.setBorder(new EmptyBorder(10,10,10,10));
        add(header, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();

        // ---------------- SEARCH TAB ----------------
        JPanel searchTab = new JPanel(new BorderLayout(10,10));
        searchTab.setBorder(new EmptyBorder(10,10,10,10));
        String[] categories = {"STANDARD", "DELUXE", "SUITE"};
        JComboBox<String> searchCombo = new JComboBox<>(categories);
        JButton searchBtn = new JButton("Search");
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Select Category: "));
        searchPanel.add(searchCombo);
        searchPanel.add(searchBtn);
        searchTab.add(searchPanel, BorderLayout.NORTH);
        searchResults.setEditable(false);
        searchResults.setFont(new Font("Monospaced", Font.PLAIN, 14));
        searchTab.add(new JScrollPane(searchResults), BorderLayout.CENTER);

        searchBtn.addActionListener(e -> {
            String category = (String) searchCombo.getSelectedItem();
            searchResults.setText("Available rooms in " + category + ":\n\n");
            rooms.stream().filter(r -> r.getCategory().equalsIgnoreCase(category))
                 .forEach(r -> searchResults.append(r + "\n"));
        });

        // ---------------- BOOK TAB ----------------
        JPanel bookTab = new JPanel(new GridBagLayout());
        bookTab.setBorder(new EmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel nameLbl = new JLabel("Guest Name: ");
        JTextField nameField = new JTextField(15);
        JLabel ciLbl = new JLabel("Check-in (yyyy-MM-dd): ");
        JTextField ciField = new JTextField(10);
        JLabel coLbl = new JLabel("Check-out (yyyy-MM-dd): ");
        JTextField coField = new JTextField(10);
        JLabel catLbl = new JLabel("Room Category: ");
        JComboBox<String> catBox = new JComboBox<>(categories);
        JButton bookBtn = new JButton("Book Room");

        gbc.gridx=0; gbc.gridy=0; bookTab.add(nameLbl, gbc);
        gbc.gridx=1; bookTab.add(nameField, gbc);
        gbc.gridx=0; gbc.gridy=1; bookTab.add(ciLbl, gbc);
        gbc.gridx=1; bookTab.add(ciField, gbc);
        gbc.gridx=0; gbc.gridy=2; bookTab.add(coLbl, gbc);
        gbc.gridx=1; bookTab.add(coField, gbc);
        gbc.gridx=0; gbc.gridy=3; bookTab.add(catLbl, gbc);
        gbc.gridx=1; bookTab.add(catBox, gbc);
        gbc.gridx=1; gbc.gridy=4; bookTab.add(bookBtn, gbc);

        bookBtn.setBackground(Color.GREEN);
        bookBtn.setForeground(Color.WHITE);
        bookBtn.setFont(new Font("Arial", Font.BOLD, 14));

        bookBtn.addActionListener(e -> {
            try {
                String guestName = nameField.getText().trim();
                LocalDate ci;
                LocalDate co;
                try {
                    ci = LocalDate.parse(ciField.getText().trim());
                    co = LocalDate.parse(coField.getText().trim());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-dd.");
                    return;
                }
                String category = (String) catBox.getSelectedItem();

                if(guestName.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter guest name"); return; }
                if(!co.isAfter(ci)) { JOptionPane.showMessageDialog(this, "Check-out must be after check-in"); return; }

                java.util.List<Room> available = rooms.stream()
                    .filter(r -> r.getCategory().equalsIgnoreCase(category))
                    .filter(r -> isRoomAvailable(r, ci, co))
                    .toList();
                if(available.isEmpty()) { JOptionPane.showMessageDialog(this, "No rooms available for selected dates."); return; }

                StringBuilder roomList = new StringBuilder();
                for(Room r: available) roomList.append(r).append("\n");
                String ridStr = JOptionPane.showInputDialog(this, "Available rooms:\n" + roomList + "\nEnter Room ID to book:");
                if(ridStr==null) return;
                int rid;
                try {
                    rid = Integer.parseInt(ridStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid Room ID"); return;
                }
                Room chosen = available.stream().filter(r->r.getId()==rid).findFirst().orElse(null);
                if(chosen==null) { JOptionPane.showMessageDialog(this,"Invalid Room ID"); return; }

                long nights = ChronoUnit.DAYS.between(ci, co);
                double total = nights * chosen.getPricePerNight();
                int confirm = JOptionPane.showConfirmDialog(this, "Book "+chosen+" for "+nights+" nights? Total = ₹"+total,"Confirm Booking", JOptionPane.YES_NO_OPTION);
                if(confirm != JOptionPane.YES_OPTION) return;

                Booking booking = new Booking(chosen, guestName, ci, co, total);
                booking.setPaid(true);
                bookings.add(booking);

                JOptionPane.showMessageDialog(this, "Booking successful!\n"+booking.getDetails());
                nameField.setText(""); ciField.setText(""); coField.setText("");
            } catch(NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format: " + ex.getMessage());
            } catch(java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format: " + ex.getMessage());
            } catch(NullPointerException ex) {
                JOptionPane.showMessageDialog(this, "A required field is missing: " + ex.getMessage());
            } catch(IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Invalid argument: " + ex.getMessage());
            }
        });

        // ---------------- CANCEL TAB ----------------
        JPanel cancelTab = new JPanel(new BorderLayout(10,10));
        cancelTab.setBorder(new EmptyBorder(10,10,10,10));
        cancelBookingsArea.setEditable(false);
        cancelBookingsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JButton cancelBtn = new JButton("Cancel Booking");
        cancelBtn.setBackground(Color.RED);
        cancelBtn.setForeground(Color.WHITE);
        cancelTab.add(new JScrollPane(cancelBookingsArea), BorderLayout.CENTER);
        cancelTab.add(cancelBtn, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> {
            cancelBookingsArea.setText("");
            for(Booking b: bookings) cancelBookingsArea.append(b.getDetails()+"\n");
            String bidStr = JOptionPane.showInputDialog(this, "Enter Booking ID to cancel:");
            if(bidStr==null) return;
            int bid;
            try {
                bid = Integer.parseInt(bidStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Booking ID"); return;
            }
            Booking found = bookings.stream().filter(b->b.getBookingId()==bid).findFirst().orElse(null);
            if(found==null) JOptionPane.showMessageDialog(this,"Booking not found");
            else {
                bookings.remove(found);
                JOptionPane.showMessageDialog(this,"Booking ID "+bid+" cancelled");
                cancelBookingsArea.setText("");
                for(Booking b: bookings) cancelBookingsArea.append(b.getDetails()+"\n");
            }
        });

        // ---------------- VIEW BOOKINGS TAB ----------------
        JPanel viewTab = new JPanel(new BorderLayout(10,10));
        viewTab.setBorder(new EmptyBorder(10,10,10,10));
        viewBookingsArea.setEditable(false);
        viewBookingsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JButton refreshBtn = new JButton("Refresh Bookings");
        refreshBtn.setBackground(Color.BLUE);
        refreshBtn.setForeground(Color.WHITE);
        viewTab.add(new JScrollPane(viewBookingsArea), BorderLayout.CENTER);
        viewTab.add(refreshBtn, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> {
            viewBookingsArea.setText("");
            for(Booking b: bookings) viewBookingsArea.append(b.getDetails()+"\n");
        });

        // Add tabs
        tabs.addTab("Search Rooms", searchTab);
        tabs.addTab("Book Room", bookTab);
        tabs.addTab("Cancel Booking", cancelTab);
        tabs.addTab("View Bookings", viewTab);

        add(tabs, BorderLayout.CENTER);
    }

    // Helper method to check room availability
    private boolean isRoomAvailable(Room room, LocalDate checkIn, LocalDate checkOut) {
        for (Booking b : bookings) {
            if (b.getRoom().getId() == room.getId()) {
                // Overlapping booking
                if (!(checkOut.isBefore(b.checkIn) || checkIn.isAfter(b.checkOut.minusDays(1)))) {
                    return false;
                }
            }
        }
        return true;
    }

    private void loadRooms() {
        rooms.add(new Room(101, "STANDARD", 2000, "Single bed, city view"));
        rooms.add(new Room(102, "STANDARD", 2200, "Double bed, city view"));
        rooms.add(new Room(201, "DELUXE", 3500, "King bed, sea view"));
        rooms.add(new Room(202, "DELUXE", 3800, "Twin bed, mountain view"));
        rooms.add(new Room(301, "SUITE", 6000, "Luxury suite, sea view, balcony"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HotelReservationGUI().setVisible(true));
    }
}