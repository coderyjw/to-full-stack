package testdemo;
import java.util.Scanner;

public class Demo {
    static void main() {
        Scanner sc = new Scanner(System.in);
        int month = sc.nextInt();
        String season = "";
        switch (month) {
            case 12, 1, 2 -> season = "冬季";
            case 3, 4, 5 -> season = "春季";
            case 6, 7, 8 -> season = "夏季";
            case 9, 10, 11 -> season = "秋季";
            default -> System.out.println("输入有误");
        }
        System.out.println(season);
    }
}
