import java.sql.*;
import java.io.*;
import oracle.jdbc.*;
import oracle.sql.*;
import java.util.*;

public class Part2 {
    private static Connection conn = null;

    public void open_branch(String address) {
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery("SELECT B#, Address FROM Branch ORDER BY B#");
            int branchCounter = get_proper_number(rs, "B#");
            rs.moveToInsertRow();
            rs.updateString("B#", String.format("%03d", branchCounter));
            rs.updateString("Address", address);
            rs.insertRow();
            System.out.println("Branch Opened.");
            stmt.close();
            rs.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Open Branch.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void close_branch(String branch) {
        boolean isDeleted = false;
        boolean isBNum = branch.chars().allMatch(Character::isDigit);
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT B#, Address FROM Branch WHERE "
                            + (isBNum ? "B#" : "Address") + " = ?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            stmt.setString(1, branch);
            ResultSet rs = stmt.executeQuery();

            rs.first();
            rs.beforeFirst();
            while (rs.next()) {
                rs.deleteRow();
                isDeleted = true;
            }
            if (!isDeleted)
                throw new Exception("Branch not exist");
            else
                System.out.println("Branch closed!");
            rs.close();
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Close Branch.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void setup_account(String name, String branch, float amount) {
        String customerNumber = null;
        boolean isBNum = branch.chars().allMatch(Character::isDigit);
        try {
            if (!isBNum)
                branch = get_branch_number(branch);
            else
                check_branch(branch);
            PreparedStatement stmt1 = conn.prepareStatement("SELECT C# FROM Customer WHERE Name = ?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            stmt1.setString(1, name);
            ResultSet rs1 = stmt1.executeQuery();
            if (rs1.first()) {
                customerNumber = rs1.getString("C#");
            } else {
                stmt1.close();
                throw new Exception("Customer not found.");
            }
            PreparedStatement stmt2 = conn.prepareStatement("SELECT B#, A#, C#, Balance FROM Account WHERE B# = ? ORDER BY A#",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            stmt2.setString(1, branch);
            ResultSet rs2 = stmt2.executeQuery();
            int accountCounter = get_proper_number(rs2, "A#");
            rs2.moveToInsertRow();
            rs2.updateString("B#", branch);
            rs2.updateString("A#", String.format("%04d", accountCounter));
            rs2.updateString("C#", customerNumber);
            rs2.updateFloat("Balance", amount);
            rs2.insertRow();
            try {
                set_status(customerNumber);
            } catch (Exception e) {
                throw e;
            }
            System.out.println("Set Account with Account# = " + branch
                    + String.format("%04d", accountCounter));
            rs1.close();
            rs2.close();
            stmt1.close();
            stmt2.close();

        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Set Account.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void setup_customer(String name, String branch) {
        setup_customer(name, branch, 0.00f);
    }

    public void setup_customer(String name, String branch, float amount) {
        boolean isBNum = branch.chars().allMatch(Character::isDigit);
        try {
            if (!isBNum)
                branch = get_branch_number(branch);
            else
                check_branch(branch);
            PreparedStatement stmt = conn.prepareStatement("SELECT C#, Name FROM Customer ORDER BY C#",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery();
            int customerCounter = get_proper_number(rs, "C#");

            rs.moveToInsertRow();
            rs.updateString("C#", String.format("%05d", customerCounter));
            rs.updateString("Name", name);
            try {
                rs.insertRow();
            } catch (Exception e) {
                throw new Exception("Customer already existed");
            }
            try {
                setup_account(name, branch, amount);
            } catch (Exception e) {
                throw e;
            }
            System.out.println("Customer set.");
            rs.close();
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to setup customer.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void close_account(String name, String branch) {
        boolean isDeleted = false;
        boolean isBNum = branch.chars().allMatch(Character::isDigit);
        try {
            if (!isBNum)
                branch = get_branch_number(branch);
            else
                check_branch(branch);
            PreparedStatement stmt1 = conn.prepareStatement("SELECT Account.B#||Account.A# AS Account# "
                            + "FROM Account, Customer WHERE Account.C# = Customer.C# AND "
                            + "Account.Balance = 0 AND Account.B# = ? AND Customer.Name = ?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            stmt1.setString(1, branch);
            stmt1.setString(2, name);
            ResultSet rs1 = stmt1.executeQuery();
            PreparedStatement stmt2 = conn.prepareStatement("DELETE FROM ACCOUNT WHERE B#||A# = ?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            rs1.beforeFirst();
            while (rs1.next()) {
                stmt2.setString(1, rs1.getString("Account#"));
                stmt2.executeUpdate();
                isDeleted = true;
            }
            if (isDeleted) {
                PreparedStatement stmt3 = conn.prepareStatement("SELECT Count(Account.C#) FROM Account, Customer WHERE "
                                + "Account.C# = Customer.C# AND Customer.Name = ?",
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
                stmt3.setString(1, name);
                ResultSet rs2 = stmt3.executeQuery();
                rs2.beforeFirst();
                while (rs2.next()) {
                    if (rs2.getInt(1) <= 0) {
                        PreparedStatement stmt4 = conn.prepareStatement("DELETE FROM CUSTOMER WHERE Name = ?",
                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_UPDATABLE);
                        stmt4.setString(1, name);
                        stmt4.executeUpdate();
                        System.out.println("Customer deleted!");
                    }
                }

                PreparedStatement stmt4 = conn.prepareStatement("SELECT Count(B#) FROM Account WHERE B# = ?",
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
                stmt4.setString(1, branch);
                ResultSet rs3 = stmt4.executeQuery();
                rs3.beforeFirst();
                while (rs3.next()) {
                    if (rs3.getInt(1) < 1) {
                        try {
                            close_branch(branch);
                        } catch (Exception e) {
                            throw e;
                        }
                    }
                }
                System.out.println("Account closed!");
                rs1.close();
                rs2.close();
                rs3.close();
                stmt1.close();
                stmt2.close();
                stmt3.close();
                stmt4.close();
            } else
                throw new Exception("None of the accounts can be closed.");
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Close Account(s).\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void withdraw(String name, String account, float amount) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT Balance, C# FROM Account WHERE B#||A# = ?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            stmt.setString(1, account);
            ResultSet rs = stmt.executeQuery();
            if (rs.first()) {
                float balance = rs.getFloat("Balance");
                if (balance >= amount) {
                    if (amount >= 0) {
                        update_balance(rs, balance, 0.00f-amount);
                    } else
                        throw new Exception("Invalid amount.");
                } else
                    throw new Exception("Insufficient funds");
            } else
                throw new Exception("Invalid account number");
            System.out.println("Withdraw complete!");
            rs.close();
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Withdraw.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void deposit(String name, String account, float amount) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT Balance, C# FROM Account WHERE B#||A# = ?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            stmt.setString(1, account);
            ResultSet rs = stmt.executeQuery();
            if (rs.first()) {
                float balance = rs.getFloat("Balance");
                if (amount >= 0) {
                    update_balance(rs, balance, amount);
                } else
                    throw new Exception("Invalid amount.");
            } else
                throw new Exception("Invalid account number");
            System.out.println("Deposit complete!");
            rs.close();
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Deposit.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void transfer(String name, String account1, String account2, float amount) {
        try {
            PreparedStatement stmt1 = conn.prepareStatement("SELECT Balance, C# FROM Account WHERE B#||A# = ?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            stmt1.setString(1, account1);
            ResultSet rs1 = stmt1.executeQuery();

            PreparedStatement stmt2 = conn.prepareStatement("SELECT Balance, C# FROM Account WHERE B#||A# = ?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            stmt2.setString(1, account2);
            ResultSet rs2 = stmt2.executeQuery();

            if (rs1.first() && rs2.first()) {
                float balance1 = rs1.getFloat("Balance");
                float balance2 = rs2.getFloat("Balance");
                if (balance1 >= amount) {
                    if (amount >= 0) {
                        update_balance(rs1, balance1, 0.00f-amount);
                        update_balance(rs2, balance2, amount);
                    } else
                        throw new Exception("Invalid amount.");
                } else
                    throw new Exception("Insufficient funds");
            } else
                throw new Exception("Invalid account number");
            System.out.println("Transfer complete!");
            rs1.close();
            rs2.close();
            stmt1.close();
            stmt2.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Withdraw.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void show_branch(String branch) {
        float total = 0;
        boolean isBNum = branch.chars().allMatch(Character::isDigit);
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT Account.B#||Account.A# AS Account#, Account.C#, Account.Balance "
                            + "FROM Branch, Account WHERE Branch.B# = Account.B# AND Branch."
                            + (isBNum ? "B#" : "Address") + " = ? ORDER BY Account.C#",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            stmt.setString(1, branch);
            ResultSet rs = stmt.executeQuery();

            System.out.println("+==========+===========+=========+");
            System.out.println("| Account# | Customer# | Balance |");
            System.out.println("+==========+===========+=========+");

            rs.beforeFirst();
            while (rs.next()) {
                System.out.format("|%10s|%11s|%9.2f|\n",
                        rs.getString("Account#"),
                        rs.getString("C#"),
                        rs.getFloat("Balance"));
                total += rs.getFloat("Balance");
                System.out.println("+----------+-----------+---------+");
            }
            System.out.format("|        Total Balance = %8.2f|\n", total);
            System.out.println("+==========+===========+=========+");
            rs.close();
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to show branch.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void show_all_branches() {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT B#, Address FROM Branch ORDER BY B#",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery();

            System.out.println("================+=================");
            System.out.println("|    Branch#    |     Address    |");
            System.out.println("================+=================");

            rs.beforeFirst();
            while (rs.next()) {
                System.out.format("|%15s|%16s|\n",
                        rs.getString("B#"),
                        rs.getString("Address"));
                show_branch(rs.getString("B#"));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to show all branch(es).\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void show_customer(String name) {
        float total = 0;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT Customer.C#, Customer.Name, Customer.Status, "
                            + "Account.B#||Account.A# AS Account#, Account.Balance "
                            + "FROM Customer, Account WHERE Customer.C# = Account.C# AND Customer.Name = ?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            System.out.println("+===========+==========+========+==========+=========+");
            System.out.println("| Customer# |   Name   | Status | Account# | Balance |");
            System.out.println("+===========+==========+========+==========+=========+");

            rs.beforeFirst();
            while (rs.next()) {
                System.out.format("|%11s|%10s|%8d|%10s|%9.2f|\n",
                        rs.getString("C#"),
                        rs.getString("Name"),
                        rs.getInt("Status"),
                        rs.getString("Account#"),
                        rs.getFloat("Balance"));
                total += rs.getFloat("Balance");
                System.out.println("+-----------+----------+--------+----------+---------+");
            }
            System.out.format("|                            Total Balance = %8.2f|\n", total);
            System.out.println("+===========+==========+========+==========+=========+");
            rs.close();
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to show customer.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    private void roll_back() {
        if (conn != null) {
            try {
                conn.rollback();
                System.out.println("Rolled Back!");
            } catch (SQLException ex) {
                System.out.println("SQL exception: ");
                ex.printStackTrace();
            }
        }
    }

    private void set_status(String cNum) {
        try {
            PreparedStatement stmt1 = conn.prepareStatement("SELECT Account.C#, SUM(Account.balance) AS TotalBalance "
                            + "FROM Account WHERE Account.C# = ? GROUP BY(Account.C#)",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            stmt1.setString(1, cNum);
            ResultSet rs = stmt1.executeQuery();
            rs.beforeFirst();
            while (rs.next()) {
                int status = 0;
                if (rs.getFloat("TotalBalance") == 0)
                    status = 0;
                else if (rs.getFloat("TotalBalance") >= 3000)
                    status = 3;
                else
                    status = (int) (rs.getFloat("TotalBalance") / 1000 + 1);

                PreparedStatement stmt2 = conn.prepareStatement("UPDATE Customer SET Status = ? WHERE C# = ?",
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
                stmt2.setInt(1, status);
                stmt2.setString(2, cNum);
                stmt2.executeUpdate();
            }
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to change status.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    private void check_branch(String bNum) throws Exception {
        PreparedStatement stmt = conn.prepareStatement("SELECT B# FROM Branch WHERE B# = ?",
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        stmt.setString(1, bNum);
        ResultSet rs = stmt.executeQuery();
        if (!rs.first())
            throw new Exception("Branch not found.");
        rs.close();
        stmt.close();
    }

    private void update_balance(ResultSet rs, float balance, float amount) throws SQLException {
        rs.updateFloat("Balance", balance + amount);
        rs.updateRow();
        try {
            set_status(rs.getString("C#"));
        } catch (Exception e) {
            throw e;
        }
    }

    private String get_branch_number(String branch) throws Exception {
        PreparedStatement stmt = conn.prepareStatement("SELECT B# FROM Branch WHERE Address = ?",
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        stmt.setString(1, branch);
        ResultSet rs = stmt.executeQuery();

        if (rs.first()) {
            branch = rs.getString("B#");
        } else {
            stmt.close();
            throw new Exception("Branch not found.");
        }
        stmt.close();
        return branch;
    }

    private int get_proper_number(ResultSet rs, String column) throws SQLException {
        int counter = 0;
        rs.beforeFirst();
        while (rs.next()) {
            if (counter == Integer.parseInt(rs.getString(column)))
                counter++;
            else
                break;
        }
        return counter;
    }

    private void prompt() {
        int choice = 0;
        int prevChoice = 0;
        do {
            prevChoice = choice;
            System.out.print("\f");
            String branch = null;
            String name = null;
            Float amount = 0.0f;
            String account = null;
            String account1 = null;
            String account2 = null;

            try {
                switch (choice) {
                    case 1:
                        System.out.println("1. open_branch");
                        String address = System.console().readLine("Address: ");
                        open_branch(address);
                        break;
                    case 2:
                        System.out.println("2. close_branch");
                        branch = System.console().readLine("Address or Branch Number: ");
                        close_branch(branch);
                        break;
                    case 3:
                        System.out.println("3. setup_account");
                        name = System.console().readLine("Name: ");
                        branch = System.console().readLine("Address or Branch Number: ");
                        amount = Float.parseFloat(System.console().readLine("Amount: "));
                        setup_account(name, branch, amount);
                        break;
                    case 4:
                        System.out.println("4. setup_customer");
                        name = System.console().readLine("Name: ");
                        branch = System.console().readLine("Address or Branch Number: ");
                        setup_customer(name, branch);
                        break;
                    case 5:
                        System.out.println("5. close_account");
                        name = System.console().readLine("Name: ");
                        branch = System.console().readLine("Address or Branch Number: ");
                        close_account(name, branch);
                        break;
                    case 6:
                        System.out.println("6. withdraw");
                        name = System.console().readLine("Name: ");
                        account = System.console().readLine("Account Number: ");
                        amount = Float.parseFloat(System.console().readLine("Amount: "));
                        withdraw(name, account, amount);
                        break;
                    case 7:
                        System.out.println("7. deposit");
                        name = System.console().readLine("Name: ");
                        account = System.console().readLine("Account Number: ");
                        amount = Float.parseFloat(System.console().readLine("Amount: "));
                        deposit(name, account, amount);
                        break;
                    case 8:
                        System.out.println("8. transfer");
                        name = System.console().readLine("Name: ");
                        account1 = System.console().readLine("Account Number from: ");
                        account2 = System.console().readLine("Account Number to: ");
                        amount = Float.parseFloat(System.console().readLine("Amount: "));
                        transfer(name, account1, account2, amount);
                        break;
                    case 9:
                        System.out.println("9. show_branch");
                        branch = System.console().readLine("Address or Branch Number: ");
                        show_branch(branch);
                        break;
                    case 10:
                        System.out.println("10. show_all_branches");
                        show_all_branches();
                        break;
                    case 11:
                        System.out.println("11. show_customer");
                        name = System.console().readLine("Name: ");
                        show_customer(name);
                        break;
                    case 0:
                        System.out.println("Main Menu - Welcome");
                        System.out.println("1. open_branch");
                        System.out.println("2. close_branch");
                        System.out.println("3. setup_account");
                        System.out.println("4. setup_customer");
                        System.out.println("5. close_account");
                        System.out.println("6. withdraw");
                        System.out.println("7. deposit");
                        System.out.println("8. transfer");
                        System.out.println("9. show_branch");
                        System.out.println("10. show_all_branches");
                        System.out.println("11. show_customer");
                        System.out.println("Any other Integer to Quit");
                        choice = Integer.parseInt(System.console().readLine("Choose what to action to preform: "));
                        break;
                }
            } catch (Exception e) {
            }
            if (prevChoice != 0) {
                choice = 0;
                System.out.println("Press any key to continue...");
                try {
                    System.in.read();
                } catch (Exception e) {
                }
            }
        } while (choice >= 0 && choice <= 11);
    }

    public void connect() {
        try {
            DriverManager.registerDriver
                    (new oracle.jdbc.driver.OracleDriver());
            System.out.println("Connecting to JDBC...");
            conn = DriverManager.getConnection
                    ("jdbc:oracle:thin:@//localhost:1521/orcl", "system", "admin");
            System.out.println("JDBC connected.\n");
        } catch (Exception e) {
            System.out.println("SQL exception: ");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void disconnect() {
        try {
            conn.close();
        } catch (Exception e) {
            System.out.println("SQL exception: ");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        Part2 p2 = new Part2();
        p2.connect();
        p2.prompt();
        p2.disconnect();
    }
}