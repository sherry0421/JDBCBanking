import java.sql.*;
import java.util.Arrays;


public class Part3 {
    private static Connection conn = null;

    public String branches(String address) throws Exception{
        CallableStatement stmt = conn.prepareCall
                ( "{? = call bank.branches(?)}" ) ;
        stmt.registerOutParameter(1, Types.CHAR);
        stmt.setString(2, address);
        try {
            stmt.execute();
            if(stmt.getString(1)==null){
                throw new Exception("Branch not exists.");
            }
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to get branch number.\n" + e.getMessage());
            System.exit(-1);
        }
        return stmt.getString(1);
    }

    public void open_branch(String address) {
        try {
            CallableStatement stmt = conn.prepareCall
                    ( "{? = call bank.open_branch(?)}" ) ;
            stmt.registerOutParameter(1, Types.VARCHAR);
            stmt.setString(2, address);
            stmt.execute();
            if(stmt.getString(1)==null){
                throw new Exception("Branch already exists.");
            }
            System.out.println("Branch Opened.");
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Open Branch.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void close_branch(String address) {
        try {
            CallableStatement stmt = conn.prepareCall
                    ( "{? = call bank.close_branch(?)}" ) ;
            stmt.registerOutParameter(1, Types.VARCHAR);
            stmt.setString(2, address);
            stmt.execute();
            if(stmt.getString(1)==null){
                throw new Exception("Branch does not exist.");
            }
            System.out.println("Branch Closed.");
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Close Branch.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void create_customer(String name) {
        try {
            CallableStatement stmt = conn.prepareCall
                    ( "{? = call bank.create_customer(?)}" ) ;
            stmt.registerOutParameter(1, Types.VARCHAR);
            stmt.setString(2, name);
            stmt.execute();
            if(stmt.getString(1)==null){
                throw new Exception("Customer already exists.");
            }
            System.out.println("Customer created.");
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Create Customer.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void remove_customer(String name) {
        try {
            CallableStatement stmt = conn.prepareCall
                    ( "{? = call bank.remove_customer(?)}" ) ;
            stmt.registerOutParameter(1, Types.VARCHAR);
            stmt.setString(2, name);
            stmt.execute();
            if(stmt.getString(1)==null){
                throw new Exception("Customer still have account or Customer does not exist.");
            }
            System.out.println("Customer removed.");
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Remove Customer.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void open_account(String name, String addr, float amount) {
        try {
            CallableStatement stmt = conn.prepareCall
                    ( "{? = call bank.open_account(?,?,?)}" ) ;
            stmt.registerOutParameter(1, Types.CHAR);
            stmt.setString(2, name);
            stmt.setString(3,addr);
            stmt.setFloat(4,amount);
            stmt.execute();
            if(stmt.getString(1)==null){
                throw new Exception("Invalid Customer, Branch or amount.");
            }
            System.out.println("Account created.");
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Create Account.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void close_account(String acct) {
        try {
            CallableStatement stmt = conn.prepareCall
                    ( "{? = call bank.close_account(?)}" ) ;
            stmt.registerOutParameter(1, Types.CHAR);
            stmt.setString(2, acct);
            stmt.execute();
            if(stmt.getString(1)==null){
                throw new Exception("Invalid Account or amount greater than 0.");
            }
            System.out.println("Account closed.");
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Close Account.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void withdraw(String acct, float amount) {
        try {
            CallableStatement stmt = conn.prepareCall
                    ( "{? = call bank.withdraw(?,?)}" ) ;
            stmt.registerOutParameter(1, Types.CHAR);
            stmt.setString(2, acct);
            stmt.setFloat(3, amount);
            stmt.execute();
            if(stmt.getString(1)==null){
                throw new Exception("Invalid Account or insufficient fund.");
            }
            System.out.println("Withdraw succeed.");
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Withdraw.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void deposit(String acct, float amount) {
        try {
            CallableStatement stmt = conn.prepareCall
                    ( "{? = call bank.deposit(?,?)}" ) ;
            stmt.registerOutParameter(1, Types.CHAR);
            stmt.setString(2, acct);
            stmt.setFloat(3, amount);
            stmt.execute();
            if(stmt.getString(1)==null){
                throw new Exception("Invalid Account or invalid amount.");
            }
            System.out.println("Deposit succeed.");
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Deposit.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void deposit(String name, String address, float amount) {
        try {
            String bno = branches(address);
            PreparedStatement pstmt = conn.prepareStatement("SELECT Count(Account.C#) FROM Account, Customer WHERE "
                            + "Account.C# = Customer.C# AND Customer.Name = ? AND Account.B#=?",
                            ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_READ_ONLY);
            pstmt.setString(1,name);
            pstmt.setString(2,bno);
            ResultSet rs = pstmt.executeQuery();
            rs.beforeFirst();
            if(!rs.first())
                throw new Exception("Invalid account.");
            pstmt = conn.prepareStatement("SELECT Account.B#||Account.A# AS Account# FROM Account, Customer WHERE "
                            + "Account.C# = Customer.C# AND Customer.Name = ? AND Account.B#=?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            pstmt.setString(1,name);
            pstmt.setString(2,bno);
            rs = pstmt.executeQuery();
            CallableStatement stmt = conn.prepareCall
                    ( "{? = call bank.deposit(?,?)}" ) ;
            stmt.registerOutParameter(1, Types.CHAR);
            stmt.setString(2, rs.getString("Account#"));
            stmt.setFloat(3, amount);
            stmt.execute();
            if(stmt.getString(1)==null){
                throw new Exception("Invalid Account or invalid amount.");
            }
            System.out.println("Deposit succeed.");
            pstmt.close();
            rs.close();
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Deposit.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void transfer(String acct1, String acct2, float amount) {
        try {
            CallableStatement stmt = conn.prepareCall
                    ( "{? = call bank.transfer(?,?,?)}" ) ;
            stmt.registerOutParameter(1, Types.CHAR);
            stmt.setString(2, acct1);
            stmt.setString(3, acct2);
            stmt.setFloat(4, amount);
            stmt.execute();
            if(stmt.getString(1)==null){
                throw new Exception("Invalid Account or invalid amount.");
            }
            System.out.println("Transfer succeed.");
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Transfer.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void transfer(String name1, String name2, String address1, String address2, float amount) {
        try {
            String bno1 = branches(address1);
            String bno2 = branches(address2);
            PreparedStatement pstmt = conn.prepareStatement("SELECT Count(Account.C#) FROM Account, Customer WHERE "
                            + "Account.C# = Customer.C# AND Customer.Name = ? AND Account.B#=?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            pstmt.setString(1,name1);
            pstmt.setString(2,bno1);
            ResultSet rs = pstmt.executeQuery();
            if(!rs.first())
                throw new Exception("Invalid account.");
            pstmt = conn.prepareStatement("SELECT Count(Account.C#) FROM Account, Customer WHERE "
                            + "Account.C# = Customer.C# AND Customer.Name = ? AND Account.B#=?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            pstmt.setString(1,name2);
            pstmt.setString(2,bno2);
            rs = pstmt.executeQuery();
            if(!rs.first())
                throw new Exception("Invalid account.");
            pstmt = conn.prepareStatement("SELECT Account.B#||Account.A# AS Account# FROM Account, Customer WHERE "
                            + "Account.C# = Customer.C# AND Customer.Name = ? AND Account.B#=?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            pstmt.setString(1,name1);
            pstmt.setString(2,bno1);
            rs = pstmt.executeQuery();
            String acct1 = rs.getString("Account#");
            pstmt = conn.prepareStatement("SELECT Account.B#||Account.A# AS Account# FROM Account, Customer WHERE "
                            + "Account.C# = Customer.C# AND Customer.Name = ? AND Account.B#=?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            pstmt.setString(1,name2);
            pstmt.setString(2,bno2);
            rs = pstmt.executeQuery();
            String acct2 = rs.getString("Account#");
            CallableStatement stmt = conn.prepareCall
                    ( "{? = call bank.transfer(?,?,?)}" ) ;
            stmt.registerOutParameter(1, Types.CHAR);
            stmt.setString(2, acct1);
            stmt.setString(3, acct2);
            stmt.setFloat(4, amount);
            stmt.execute();
            if(stmt.getString(1)==null){
                throw new Exception("Invalid Account or invalid amount.");
            }
            System.out.println("Transfer succeed.");
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Transfer.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void show_branch(String addr) {
        try {
            CallableStatement stmt = conn.prepareCall
                    ( "{? = call bank.show_branch(?)}" ) ;
            stmt.registerOutParameter(1, Types.ARRAY, "DBMSOUTPUT_LINESARRAY");
            stmt.setString(2, addr);
            stmt.execute();
            Array array = null;
            array = stmt.getArray(1);
            if(array==null){
                throw new Exception("Invalid branch.");
            }
            String[] strArr = (String[])array.getArray();
            for (String str : strArr) {
                System.out.println(str);
            }
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Show Branch.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void show_all_branches() {
        try {
            CallableStatement stmt = conn.prepareCall
                    ( "{? = call bank.show_all_branches()}" ) ;
            stmt.registerOutParameter(1, Types.ARRAY, "DBMSOUTPUT_LINESARRAY");
            stmt.execute();
            Array array = null;
            array = stmt.getArray(1);
            String[] strArr = (String[])array.getArray();
            for (String str : strArr) {
                System.out.println(str);
            }
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Show All Branches.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public void show_customer(String name) {
        try {
            CallableStatement stmt = conn.prepareCall
                    ( "{? = call bank.show_customer(?)}" ) ;
            stmt.registerOutParameter(1, Types.ARRAY, "DBMSOUTPUT_LINESARRAY");
            stmt.setString(2, name);
            stmt.execute();
            Array array = null;
            array = stmt.getArray(1);
            if(array==null){
                throw new Exception("Invalid customer.");
            }
            String[] strArr = (String[])array.getArray();
            for (String str : strArr) {
                System.out.println(str);
            }
            stmt.close();
        } catch (Exception e) {
            roll_back();
            System.out.println("Unable to Show Customer.\n" + e.getMessage());
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
            String address = null;

            try {
                switch (choice) {
                    case 1:
                        System.out.println("1. open_branch");
                        address = System.console().readLine("Branch Address: ");
                        open_branch(address);
                        break;
                    case 2:
                        System.out.println("2. close_branch");
                        branch = System.console().readLine("Branch Address: ");
                        close_branch(branch);
                        break;
                    case 3:
                        System.out.println("3. create_customer");
                        name = System.console().readLine("Name: ");
                        create_customer(name);
                        break;
                    case 4:
                        System.out.println("4. open_account");
                        name = System.console().readLine("Name: ");
                        branch = System.console().readLine("Branch Address: ");
                        amount = Float.parseFloat(System.console().readLine("Amount: "));
                        open_account(name, branch, amount);
                        break;
                    case 5:
                        System.out.println("5. close_account");
                        account = System.console().readLine("Account Number: ");
                        close_account(account);
                        break;
                    case 6:
                        System.out.println("6. withdraw");
                        account = System.console().readLine("Account Number: ");
                        amount = Float.parseFloat(System.console().readLine("Amount: "));
                        withdraw(account, amount);
                        break;
                    case 7:
                        System.out.println("7. deposit");
                        account = System.console().readLine("Account Number: ");
                        amount = Float.parseFloat(System.console().readLine("Amount: "));
                        deposit(account, amount);
                        break;
                    case 8:
                        System.out.println("8. transfer");
                        account1 = System.console().readLine("Account Number from: ");
                        account2 = System.console().readLine("Account Number to: ");
                        amount = Float.parseFloat(System.console().readLine("Amount: "));
                        transfer(account1, account2, amount);
                        break;
                    case 9:
                        System.out.println("9. show_branch");
                        branch = System.console().readLine("Branch Address: ");
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

    public static void main(String[] args) {
        try {
            Part3 p3 = new Part3();
            p3.connect();
            p3.prompt();
            p3.disconnect();
        } catch (Exception e) {
            System.out.println("SQL exception: ");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
