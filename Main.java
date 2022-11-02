import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.lang.Math;

public class Main {

  private static String ttyConfig;

  public static void main(String[] args) {

    int frame = 0;

    int playerPaddleY = 7;
    int playerLastMove = 0;

    int computerPaddleY = 10;

    int[] ballPos = { 30, 2 };
    int[] ballVel = { -2, 1 };

    final int paddleSpeed = 3;
    int ballSpeed = 5; // Inverse of ball speed

    int dir = 0;

    boolean playerWon;

    initBoard();

    try {
      setTerminalToCBreak();

      System.out.print("\033[?25l");

      while (true) {
        frame++;

        if (System.in.available() != 0) {
          System.in.read();

          dir = arrowInput();
        }

        if (dir != 0 && frame - playerLastMove >= paddleSpeed) {
          if (dir == 1) {
            playerPaddleY--;
          } else {
            playerPaddleY++;
          }

          if (playerPaddleY < 2) {
            playerPaddleY = 2;
          } else if (playerPaddleY > 13) {
            playerPaddleY = 13;
          }

          dir = 0;
          playerLastMove = frame;
        }

        if (frame % paddleSpeed == 0) {
          computerPaddleY += computerMove(computerPaddleY, ballPos);
        }

        if (computerPaddleY < 2) {
          computerPaddleY = 2;
        } else if (computerPaddleY > 13) {
          computerPaddleY = 13;
        }

        clearArea(ballPos[0], ballPos[1], 1, 1);

        if (frame % ballSpeed == 0) {
          ballPos[0] += ballVel[0];
          ballPos[1] += ballVel[1];

          if (ballPos[1] < 2) {
            ballPos[1] = 2;
          }

          else if (ballPos[1] > 15) {
            ballPos[1] = 15;
          }

          // Bounce off top and bottom
          if (ballPos[1] == 2 || ballPos[1] == 15) {
            ballVel[1] = -ballVel[1];
          }

          // Bounce off player paddle
          if (ballPos[0] == 3) {
            if (playerPaddleY == ballPos[1] || playerPaddleY + 1 == ballPos[1]
                || playerPaddleY + 2 == ballPos[1]) {
              ballVel[0] = -ballVel[0];

              if (ballPos[0] == playerPaddleY + 1) {
                ballVel[1] = ballVel[1] / ballVel[1];
              } else {
                ballVel[1] = ballVel[1] / ballVel[1] * 2;
              }
            }
          } else if (ballPos[0] == 2) {
            if (playerPaddleY == ballPos[1] || playerPaddleY + 1 == ballPos[1]
                || playerPaddleY + 2 == ballPos[1]) {
              ballVel[0] = -ballVel[0];

            } else {
              playerWon = false;
              drawGame(playerPaddleY, computerPaddleY, ballPos);
              break;
            }
          }

          // Bounce off computer paddle
          if (ballPos[0] == 41) {
            if (computerPaddleY == ballPos[1] || computerPaddleY + 1 == ballPos[1]
                || computerPaddleY + 2 == ballPos[1]) {
              ballVel[0] = -ballVel[0];

              if (ballPos[0] == computerPaddleY + 1) {
                ballVel[1] = ballVel[1] / ballVel[1];
              } else {
                ballVel[1] = ballVel[1] / ballVel[1] * 2;
              }
            }
          } else if (ballPos[0] == 42) {
            if (computerPaddleY == ballPos[1] || computerPaddleY + 1 == ballPos[1]
                || computerPaddleY + 2 == ballPos[1]) {
              ballVel[0] = -ballVel[0];


            } else {
              playerWon = true;
              drawGame(playerPaddleY, computerPaddleY, ballPos);
              break;
            }
          }

        }

        drawGame(playerPaddleY, computerPaddleY, ballPos);

        TimeUnit.MILLISECONDS.sleep(30);
      }

      if (playerWon) {
        moveCursor(17, 17);
        System.out.print("\033[1;32mPlayer Won!\033[0m");
      } else {
        moveCursor(15, 17);
        System.out.print("\033[1;31mComputer Won!\033[0m");
      }

      System.out.print("\033[?25h");

    } catch (IOException e) {
      System.err.println("IOException");
    } catch (InterruptedException e) {
      System.err.println("InterruptedException");
    } finally {
      try {
        stty(ttyConfig.trim());
      } catch (Exception e) {
        System.err.println("Exception restoring tty config");
      }
    }
  }

  public static int arrowInput() {
    try {
      if (System.in.available() != 0) {
        System.in.read();
      }

      if (System.in.available() != 0) {
        int b = System.in.read();

        if (b == 65) {
          return 1;
        }
        if (b == 66) {
          return 2;
        }
        if (b == 67) {
          return 3;
        }
        if (b == 68) {
          return 4;
        }
      }
    } catch (IOException e) {
      System.err.println("IOException"); // very good exception handling
    }

    return 0;
  }

  public static void initBoard() {
    clearScreen();

    System.out.println("┌─────────────────────────────────────────┐");
    System.out.println("│                                         │");
    System.out.println("│                                         │");
    System.out.println("│                                         │");
    System.out.println("│                                         │");
    System.out.println("│                                         │");
    System.out.println("│                                         │");
    System.out.println("│                                         │");
    System.out.println("│                                         │");
    System.out.println("│                                         │");
    System.out.println("│                                         │");
    System.out.println("│                                         │");
    System.out.println("│                                         │");
    System.out.println("│                                         │");
    System.out.println("│                                         │");
    System.out.println("└─────────────────────────────────────────┘");
  }

  public static void drawGame(int playerPaddleY, int computerPaddleY, int[] ballPos) {
    clearArea(2, 2, 1, 14);
    printPaddle(2, playerPaddleY);

    clearArea(42, 2, 1, 14);
    printPaddle(42, computerPaddleY);

    moveCursor(ballPos[0], ballPos[1]);
    System.out.print("◯");
  }

  public static void clearScreen() {
    System.out.print("\033[H\033[2J");
    System.out.flush();
  }

  public static void clearArea(int x, int y, int width, int height) {
    for (int i = 0; i < height; i++) {
      moveCursor(x, y + i);
      System.out.print(" ".repeat(width));
    }
  }

  public static void moveCursor(int x, int y) {
    System.out.printf("\033[%d;%dH", y, x);
  }

  public static void printPaddle(int x, int y) {
    moveCursor(x, y);
    System.out.print("▐");
    moveCursor(x, y + 1);
    System.out.print("▐");
    moveCursor(x, y + 2);
    System.out.print("▐");
  }

  public static int computerMove(int computerPaddleY, int[] ballPos) {
    // 1 in 7 chance of blundering
    if (Math.random() < 0.2) {
      return 0;
    }

    if (ballPos[0] <= 21) {
      return 0;
    }

    if (ballPos[1] < computerPaddleY + 1) {
      if (Math.random() < 0.2) {
        return -2;
      }
      return -1;
    } else if (ballPos[1] > computerPaddleY + 1) {
      if (Math.random() < 0.2) {
        return 2;
      }
      return 1;
    } else {
      return 0;
    }
  }

  // All the stuff below I got from
  // https://darkcoding.net/software/non-blocking-console-io-is-not-possible/

  private static void setTerminalToCBreak() throws IOException, InterruptedException {

    ttyConfig = stty("-g");

    // set the console to be character-buffered instead of line-buffered
    stty("-icanon min 1");

    // disable character echoing
    stty("-echo");
  }

  /**
   * Execute the stty command with the specified arguments
   * against the current active terminal.
   */
  private static String stty(final String args)
      throws IOException, InterruptedException {
    String cmd = "stty " + args + " < /dev/tty";

    return exec(new String[] {
        "sh",
        "-c",
        cmd
    });
  }

  /**
   * Execute the specified command and return the output
   * (both stdout and stderr).
   */
  private static String exec(final String[] cmd)
      throws IOException, InterruptedException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();

    Process p = Runtime.getRuntime().exec(cmd);
    int c;
    InputStream in = p.getInputStream();

    while ((c = in.read()) != -1) {
      bout.write(c);
    }

    in = p.getErrorStream();

    while ((c = in.read()) != -1) {
      bout.write(c);
    }

    p.waitFor();

    String result = new String(bout.toByteArray());
    return result;
  }

}