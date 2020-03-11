import java.sql.Connection;

public class Part5 {
    public static Connection conn = null;

    public static void main(String[] args) {
        Part4 p4 = new Part4();

        p4.connect();
        p4.createDB();

        System.out.println("1. open a branch in London");
        p4.open_branch("London");

        System.out.println("2. open a branch in Paris");
        p4.open_branch("Paris");

        System.out.println("3. open a branch in Toronto");
        p4.open_branch("Toronto");

        System.out.println("4. open a branch in New York");
        p4.open_branch("New York");

        System.out.println("5. show all branches");
        p4.show_all_branches();

        System.out.println("6. setup a customer called John in London branch");
        p4.setup_customer("John", "London");

        System.out.println("7. setup an account for John in Toronto branch with initial deposit of $500");
        p4.setup_account("John", "Toronto", 500);

        System.out.println("8. setup an account for John in Paris branch with initial deposit of $1,000");
        p4.setup_account("John", "Paris", 1000);

        System.out.println("9. setup an account for John in New York branch with initial deposit of $1,000");
        p4.setup_account("John", "New York", 1000);

        System.out.println("10. show customer John");
        p4.show_customer("John");

        System.out.println("11. setup a customer called Joan in Toronto branch with initial deposit of $1,000");
        p4.setup_customer("Joan", "Toronto", 1000);

        System.out.println("12. setup a customer called Mary in Paris branch");
        p4.setup_customer("Mary", "Paris");

        System.out.println("13. deposit $1,000 to Mary's Paris branch");
        p4.deposit("Mary", "0010001", 1000);

        System.out.println("14. setup a customer called Mary in New York branch with initial deposit of $1,000");
        p4.setup_account("Mary", "New York", 1000);

        System.out.println("15. setup an account for Mary in New York branch with initial deposit of $1,000");
        p4.setup_account("Mary", "New York", 1000);

        System.out.println("16. show customer Mary");
        //p4.show_customer("Mary");

        System.out.println("17. setup a customer called Sean in Toronto branch with initial deposit of $1,000");
        p4.setup_customer("Sean", "Toronto", 1000);

        System.out.println("18. setup a customer called Tony in Ottawa branch");
        //p4.setup_customer("Tony", "Ottawa");

        System.out.println("19. setup a customer called Tony in Toronto branch");
        p4.setup_customer("Tony", "Toronto");

        System.out.println("20. transfer $1000 from John's Toronto account to his Paris account");
        //p4.transfer("John", "0020000", "0010000", 1000);

        System.out.println("21. transfer $1000 from John’s New York branch to his Paris account");
        p4.transfer("John", "0030000", "0010000", 1000);

        System.out.println("22. show the balances of all John’s account.");
        //p4.show_customer("John");

        System.out.println("23. close every John's account that has 0 balance.");
        p4.close_account("John", "London");
        p4.close_account("John", "New York");

        System.out.println("24. withdraw $1000 from Sean's Toronto account");
        p4.withdraw("Sean", "0020002", 1000);

        System.out.println("25. show all accounts in Toronto branch");
        p4.show_branch("Toronto");

        System.out.println("26. show all branches of the bank.");
        p4.show_all_branches();

        p4.disconnect();
    }
}
