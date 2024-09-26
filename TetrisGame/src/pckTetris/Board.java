package pckTetris;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.sound.sampled.*;
import javax.swing.*;

public class Board extends JPanel implements KeyListener, MouseListener, MouseMotionListener {

    private static final long serialVersionUID = 1L;
    private Clip music;
    private FloatControl musicVolume;
    private BufferedImage blocks, background, pause, refresh, musicOn, musicOff;
    private final int boardHeight = 20, boardWidth = 10;
    private final int blockSize = 30;
    private int[][] board = new int[boardHeight][boardWidth];
    private Shape[] shapes = new Shape[7];
    private static Shape currentShape, nextShape1, nextShape2;
    private Timer looper;
    private int FPS = 60;
    private int delay = 1000 / FPS;
    private int mouseX, mouseY;
    private boolean leftClick = false;
    private Rectangle stopBounds, refreshBounds, musicBounds;
    private boolean gamePaused = false;
    private boolean gameOver = false;
    private Timer buttonLapse = new Timer(300, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            buttonLapse.stop();
        }
    });
    private boolean musicPlaying = true;
    private int score = 0;

    public Board() {
        blocks = ImageLoader.loadImage("/tiles.png");
        background = ImageLoader.loadImage("/background.png");
        pause = ImageLoader.loadImage("/pause.png");
        refresh = ImageLoader.loadImage("/refresh.png"); 
        musicOn = ImageLoader.loadImage("/music1.png");
        musicOff = ImageLoader.loadImage("/music2.png");
        music = ImageLoader.loadSound("/music.wav");
        musicVolume = ImageLoader.getVolumeControl(music);
        music.loop(Clip.LOOP_CONTINUOUSLY);
        mouseX = 0;
        mouseY = 0;
        
        stopBounds = new Rectangle(350, 500, pause.getWidth(), pause.getHeight() + pause.getHeight()/2);
        refreshBounds = new Rectangle(350, 500 - refresh.getHeight() - 20, refresh.getWidth(), refresh.getHeight() + refresh.getHeight() / 2);
        musicBounds = new Rectangle(350, 500 - refresh.getHeight() - musicOn.getHeight() - 40, musicOn.getWidth(), musicOn.getHeight());
        
        looper = new Timer(delay, new GameLooper());
        
        
        
        shapes[0] = new Shape(new int[][] { { 1, 1, 1, 1 } }, blocks.getSubimage(0, 0, blockSize, blockSize), this, 1);
        shapes[1] = new Shape(new int[][] { { 1, 1, 1 }, { 0, 1, 0 } }, blocks.getSubimage(blockSize, 0, blockSize, blockSize), this, 2);
        shapes[2] = new Shape(new int[][] { { 1, 1, 1 }, { 1, 0, 0 } }, blocks.getSubimage(blockSize * 2, 0, blockSize, blockSize), this, 3);
        shapes[3] = new Shape(new int[][] { { 1, 1, 1 }, { 0, 0, 1 } }, blocks.getSubimage(blockSize * 3, 0, blockSize, blockSize), this, 4);
        shapes[4] = new Shape(new int[][] { { 0, 1, 1 }, { 1, 1, 0 } }, blocks.getSubimage(blockSize * 4, 0, blockSize, blockSize), this, 5);
        shapes[5] = new Shape(new int[][] { { 1, 1, 0 }, { 0, 1, 1 } }, blocks.getSubimage(blockSize * 5, 0, blockSize, blockSize), this, 6);
        shapes[6] = new Shape(new int[][] { { 1, 1 }, { 1, 1 } }, blocks.getSubimage(blockSize * 6, 0, blockSize, blockSize), this, 7);
    }

    private void update() {
        if (stopBounds.contains(mouseX, mouseY) && leftClick && !buttonLapse.isRunning() && !gameOver) {
            buttonLapse.start();
            gamePaused = !gamePaused;
        }
        if (refreshBounds.contains(mouseX, mouseY) && leftClick)
            startGame();
        if (musicBounds.contains(mouseX, mouseY) && leftClick && !buttonLapse.isRunning()) {
            buttonLapse.start();
            if (musicPlaying) {
                musicVolume.setValue(musicVolume.getMinimum()); // Mute the music
            } else {
                musicVolume.setValue(0); // Unmute the music (0 is the nominal value, can be adjusted as needed)
            }
            musicPlaying = !musicPlaying;
        }
        if (gamePaused || gameOver) {
            return;
        }
        currentShape.update();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, null);

        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] != 0) {
                    g.drawImage(blocks.getSubimage((board[row][col] - 1) * blockSize, 0, blockSize, blockSize), col * blockSize, row * blockSize, null);
                }
            }
        }

        // Draw the nextShape1 in the preview area
        for (int row = 0; row < nextShape1.getCoords().length; row++) {
            for (int col = 0; col < nextShape1.getCoords()[0].length; col++) {
                if (nextShape1.getCoords()[row][col] != 0) {
                    g.drawImage(nextShape1.getBlock(), col * 30 + 320, row * 30 + 50, null);
                }
            }
        }

        // Draw the nextShape2 in the preview area
        for (int row = 0; row < nextShape2.getCoords().length; row++) {
            for (int col = 0; col < nextShape2.getCoords()[0].length; col++) {
                if (nextShape2.getCoords()[row][col] != 0) {
                    g.drawImage(nextShape2.getBlock(), col * 30 + 320, row * 30 + 150, null); // Adjust the position as needed
                }
            }
        }

        currentShape.render(g);

        if (stopBounds.contains(mouseX, mouseY)) {
            g.drawImage(pause.getScaledInstance(pause.getWidth() + 2, pause.getHeight() + 2, BufferedImage.SCALE_DEFAULT), stopBounds.x + 2, stopBounds.y + 2, null);
        } else {
            g.drawImage(pause, stopBounds.x, stopBounds.y, null);
        }

        if (refreshBounds.contains(mouseX, mouseY)) {
            g.drawImage(refresh.getScaledInstance(refresh.getWidth() + 2, refresh.getHeight() + 2, BufferedImage.SCALE_DEFAULT), refreshBounds.x + 2, refreshBounds.y + 2, null);
        } else {
            g.drawImage(refresh, refreshBounds.x, refreshBounds.y, null);
        }

        if (musicBounds.contains(mouseX, mouseY)) {
            g.drawImage((musicPlaying ? musicOn : musicOff).getScaledInstance(musicOn.getWidth() + 2, musicOn.getHeight() + 3, BufferedImage.SCALE_DEFAULT), musicBounds.x + 2, musicBounds.y + 2, null);
        } else {
            g.drawImage(musicPlaying ? musicOn : musicOff, musicBounds.x, musicBounds.y, null);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Verdana", Font.BOLD, 20));
        g.drawString("SCORE:", Window.WIDTH - 125, Window.HEIGHT / 2 - 30);
        g.drawString(score + "", Window.WIDTH - 125, Window.HEIGHT / 2);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.decode("#212121"));

        for (int i = 0; i <= boardHeight; i++) {
            g2d.drawLine(0, i * blockSize, boardWidth * blockSize, i * blockSize);
        }

        for (int j = 0; j <= boardWidth; j++) {
            g2d.drawLine(j * blockSize, 0, j * blockSize, boardHeight * 30);
        }

        if (gamePaused) {
            String gamePausedString = "GAME PAUSED";
            g.setColor(Color.WHITE);
            g.setFont(new Font("Verdana", Font.BOLD, 30));
            g.drawString(gamePausedString, 35, Window.HEIGHT / 2);
        }

        if (gameOver) {
            String gameOverString = "GAME OVER";
            g.setColor(Color.WHITE);
            g.setFont(new Font("Verdana", Font.BOLD, 30));
            g.drawString(gameOverString, 50, Window.HEIGHT / 2);
        }
    }


    public void setNextShapes() {
        int index1 = (int) (Math.random() * shapes.length);
        int index2 = (int) (Math.random() * shapes.length);
        
        // Ensure that the two indices are different
        while (index2 == index1) {
            index2 = (int) (Math.random() * shapes.length);
        }
        
        nextShape1 = new Shape(shapes[index1].getCoords(), shapes[index1].getBlock(), this, shapes[index1].getColor());
        nextShape2 = new Shape(shapes[index2].getCoords(), shapes[index2].getBlock(), this, shapes[index2].getColor());
    }


    public void setCurrentShape() {
        currentShape = nextShape1;
        nextShape1 = nextShape2;
        
        int index = (int) (Math.random() * shapes.length);
        nextShape2 = new Shape(shapes[index].getCoords(), shapes[index].getBlock(), this, shapes[index].getColor());
        
        for (int row = 0; row < currentShape.getCoords().length; row++) {
            for (int col = 0; col < currentShape.getCoords()[0].length; col++) {
                if (currentShape.getCoords()[row][col] != 0) {
                    if (board[currentShape.getY() + row][currentShape.getX() + col] != 0)
                        gameOver = true;
                }
            }
        }
    }


    public int[][] getBoard() {
        return board;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gamePaused || gameOver) {
            return; // Ignore key presses if the game is paused or over
        }

        if (e.getKeyCode() == KeyEvent.VK_UP)
            currentShape.rotateShape();
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            currentShape.setDeltaX(1);
            currentShape.update();
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            currentShape.setDeltaX(-1);
            currentShape.update();
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN)
            currentShape.speedUp();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gamePaused || gameOver) {
            return; // Ignore key releases if the game is paused or over
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN)
            currentShape.speedDown();
    }


    @Override
    public void keyTyped(KeyEvent e) {
    }

    public void startGame() {
        stopGame();
        setNextShapes();
        setCurrentShape();
        gameOver = false;
        looper.start();
    }

    public void stopGame() {
        score = 0;
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                board[row][col] = 0;
            }
        }
        looper.stop();
    }

    class GameLooper implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            update();
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1)
            leftClick = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1)
            leftClick = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public void addScore() {
        score++;
    }
}
