import java.sql.*;
import java.io.*;
import oracle.jdbc.*;
import oracle.sql.*;
import java.util.*;

public class Part4 {
    public static void main(String[] args) {
        Part3 p3 = new Part3();
        p3.connect();
        System.out.println("1. open a branch in London");
        p3.open_branch("London");

        System.out.println("2. open a branch in Munich");
        p3.open_branch("Munich");

        System.out.println("3. open a branch in New York");
        p3.open_branch("New York");

        System.out.println("4. open a branch in Toronto");
        p3.open_branch("Toronto");

        System.out.println("5. create a customer Adams");
        p3.create_customer("Adams");

        System.out.println("6. create a customer Blake");
        p3.create_customer("Blake");

        System.out.println("7. create a customer Henry");
        p3.create_customer("Henry");

        System.out.println("8. create a customer Jones");
        p3.create_customer("Jones");

        System.out.println("9. create a customer Smith");
        p3.create_customer("Smith");

        System.out.println("10. open an account for Adams in London branch with an initial deposit of $1,000");
        p3.open_account("Adams", "London", 1000);

        System.out.println("11. open an account for Adams in Munich branch with an initial deposit of $1,000");
        p3.open_account("Adams", "Munich", 1000);

        System.out.println("12. open an account for Adams in New York branch with an initial deposit of $1,000");
        p3.open_account("Adams", "New York", 1000);

        System.out.println("13. open an account for Adams in Toronto branch with an initial deposit of $1,000");
        p3.open_account("Adams", "Toronto", 1000);

        System.out.println("14. open an account for Blake in London branch with an initial deposit of $1,000");
        p3.open_account("Blake", "London", 1000);

        System.out.println("15. open an account for Blake in Munich branch with an initial deposit of $2,000");
        p3.open_account("Blake", "Munich", 2000);

        System.out.println("16. open an account for Blake in New York branch with an initial deposit of $3,000");
        p3.open_account("Blake", "New York", 3000);

        System.out.println("17. open an account for Henry in London branch with an initial deposit of $2,000");
        p3.open_account("Henry", "London", 2000);

        System.out.println("18. open an account for Henry in Munich branch with an initial deposit of $1,000");
        p3.open_account("Henry", "Munich", 1000);

        System.out.println("19. open an account for Jones in Toronto branch with an initial deposit of $5,000");
        p3.open_account("Jones", "Toronto", 5000);

        System.out.println("20. show customer Adams (not just customer table info, use PL/SQL subprograms)");
        p3.show_customer("Adams");

        System.out.println("21. show customer Blake");
        p3.show_customer("Blake");

        System.out.println("22. show customer Henry");
        p3.show_customer("Henry");

        System.out.println("23. show customer Jones");
        p3.show_customer("Jones");

        System.out.println("24. show customer Smith");
        p3.show_customer("Smith");

        System.out.println("25. show London branch");
        p3.show_branch("London");

        System.out.println("26. show Munich branch");
        p3.show_branch("Munich");

        System.out.println("27. show New York branch");
        p3.show_branch("New York");

        System.out.println("28. show Toronto Branch");
        p3.show_branch("Toronto");

        System.out.println("29. show all branches");
        p3.show_all_branches();

        System.out.println("30. deposit $1000 to Smith’s Toronto account");
        //p3.deposit("Smith", "Toronto", 1000);

        System.out.println("31. transfer $1000 from Smith’s London account to Toronto account");
        p3.transfer("Smith", "Smith", "London", "Toronto", 1000);

        System.out.println("32. transfer $1000 from Henry’s Munich account to London account");
        p3.transfer("0010002", "0000002", 1000);

        System.out.println("33. transfer $3000 from Henry’s London account to Jones’ Toronto account");
        p3.transfer("0000002", "0030001", 3000);

        System.out.println("34. transfer $1000 from Adams' London account to Munich account");
        p3.transfer("0000000", "0010000", 1000);

        System.out.println("35. transfer $1000 from Adams' New York account to Toronto account");
        p3.transfer("0020000", "0030000", 1000);

        System.out.println("36. delete Smith as a customer");
        p3.remove_customer("Smith");

        System.out.println("37. close all accounts that have balance 0 (Henry’s two account)");
        p3.close_account("0000002");
        p3.close_account("0010002");

        System.out.println("38. Show all branches (Henry’s account should be gone)");
        p3.show_all_branches();

        System.out.println("39. open an account for Jones in London branch");
        p3.open_account("Jones", "London", 0);

        System.out.println("40. show customer Jones (the account # should be the one Henry had)");
        p3.show_customer("Jones");
        p3.disconnect();
    }
}
