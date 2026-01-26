package t;
import java.util.Scanner;

public class TicTacToe {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        char[][] board = {
        		{'-', '-', '-'},
        		{'-', '-', '-'}, 
        		{'-', '-', '-'}
        		
        };
        char player = 'X';
        boolean gameOver = false;
        int moves = 0;

        System.out.println("=== لعبة XO ===");
        System.out.print("اسم اللاعب الأول (X): ");
        String player1 = scanner.nextLine();
        System.out.print("اسم اللاعب الثاني (O): ");
        String player2 = scanner.nextLine();

        while (!gameOver && moves < 9) {
            // طباعة اللوحة
            System.out.println("\n  1 2 3");
            for (int i = 0; i < 3; i++) {
                System.out.print((i + 1) + " ");
                for (int j = 0; j < 3; j++) {
                    System.out.print(board[i][j] + " ");
                }
                System.out.println();
            }

            // دور اللاعب
            String name = (player == 'X') ? player1 : player2;
            System.out.println("\nدور " + name + " (" + player + ")");
            System.out.print("اختر الصف (1-3): ");
            int row = scanner.nextInt() - 1;
            System.out.print("اختر العمود (1-3): ");
            int col = scanner.nextInt() - 1;

            // التحقق من الموقع
            if (row >= 0 && row < 3 && col >= 0 && col < 3 && board[row][col] == '-') {
                board[row][col] = player;
                moves++;

                // التحقق من الفوز
                // صفوف وأعمدة
                for (int i = 0; i < 3; i++) {
                    if ((board[i][0] == player && board[i][1] == player && board[i][2] == player) ||
                        (board[0][i] == player && board[1][i] == player && board[2][i] == player)) {
                        gameOver = true;
                    }
                }
                // أقطار
                if ((board[0][0] == player && board[1][1] == player && board[2][2] == player) ||
                    (board[0][2] == player && board[1][1] == player && board[2][0] == player)) {
                    gameOver = true;
                }

                if (gameOver) {
                    System.out.println("\n🎉 " + name + " فاز!");
                } else {
                    player = (player == 'X') ? 'O' : 'X'; // تبديل اللاعب
                }
            } else {
                System.out.println("موقع خطأ! حاول مرة أخرى");
            }
        }

        if (!gameOver) {
            System.out.println("\n⚖️ تعادل!");
        }
        
        scanner.close();
    }
}
