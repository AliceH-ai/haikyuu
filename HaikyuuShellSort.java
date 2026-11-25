import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Proyecto de aula - Estructuras de datos
 * Juego de Haikyuu!! que muestra el funcionamiento de SHELL SORT.
 *
 * Flujo: intro con dialogos -> ordenamiento paso a paso por altura ->
 *        rotacion inicial (los 6 mas altos) -> mini partido 2 vs 2 con los sobrantes.
 *
 * Compilar:  javac -encoding UTF-8 HaikyuuShellSort.java
 * Ejecutar:  java HaikyuuShellSort
 */
public class HaikyuuShellSort extends JFrame {

    // ====================================================================
    //  DATOS
    // ====================================================================
    static class Jugador {
        final String nombre;
        final double altura; // cm (aproximadas: verificar en la wiki de Haikyuu)
        final Color color;

        Jugador(String nombre, double altura, Color color) {
            this.nombre = nombre;
            this.altura = altura;
            this.color = color;
        }
    }

    static final Jugador[] PLANTEL = {
        new Jugador("Hinata", 162.8, new Color(0xEA580C)),
        new Jugador("Kageyama", 180.6, new Color(0x1E3A8A)),
        new Jugador("Tsukishima", 188.3, new Color(0xCA8A04)),
        new Jugador("Yamaguchi", 181.3, new Color(0x15803D)),
        new Jugador("Tanaka", 178.8, new Color(0x7C3AED)),
        new Jugador("Nishinoya", 159.3, new Color(0xDC2626)),
        new Jugador("Asahi", 184.7, new Color(0x0E7490)),
        new Jugador("Daichi", 176.9, new Color(0x334155)),
        new Jugador("Sugawara", 174.2, new Color(0x64748B)),
        new Jugador("Ennoshita", 175.7, new Color(0xA16207)),
    };

    // {quien habla, texto}
    static final String[][] INTRO = {
        {"Narrador", "Preparatoria Karasuno, gimnasio principal. El equipo está desordenado y hay que armar la rotación del partido… ¡según la altura de cada jugador!"},
        {"Hinata", "¡Soy Hinata Shōyō! Mido 162,8 cm: soy de los más bajos, pero salto como nadie. ¡Hoy nos ordena el algoritmo Shell Sort!"},
        {"Kageyama", "Kageyama Tobio, armador. Shell Sort es una mejora del Insertion Sort: en vez de comparar solo vecinos, compara jugadores separados por una distancia llamada gap."},
        {"Tsukishima", "Tsukishima Kei. El gap empieza grande (n/2) y se reduce a la mitad en cada ronda hasta llegar a 1. Así, los jugadores muy lejos de su lugar llegan rápido."},
        {"Daichi", "Daichi, capitán. Con gap = 1 la fila ya está casi ordenada, y solo quedan ajustes pequeños entre vecinos."},
        {"Nishinoya", "¡Nishinoya, libero! Con la secuencia n/2 el peor caso es O(n²), pero en la práctica es mucho más rápido que Insertion Sort."},
        {"Asahi", "Al terminar, los 6 más altos formarán la rotación inicial y los 4 que sobran jugarán un partido 2 contra 2."},
        {"Hinata", "¡Entonces mezclemos a todos y a ordenar! ¡Vamos, Karasuno!"},
    };

    static final Color COLOR_NARRADOR = new Color(0x78716C);
    static final Color NARANJA = new Color(0xEA580C);

    // Zonas de la cancha: la rotacion sigue este anillo (sentido del reloj)
    static final int[] ANILLO = {4, 3, 2, 1, 6, 5};
    // Como se dibujan las zonas (fila de red arriba, fondo abajo)
    static final int[] CUADRICULA = {4, 3, 2, 5, 6, 1};

    // ====================================================================
    //  SHELL SORT
    // ====================================================================

    /** Version limpia del algoritmo (la que se explica en clase). */
    static void shellSort(Jugador[] v) {
        for (int gap = v.length / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < v.length; i++) {
                for (int j = i; j >= gap && v[j - gap].altura > v[j].altura; j -= gap) {
                    Jugador t = v[j];
                    v[j] = v[j - gap];
                    v[j - gap] = t;
                }
            }
        }
    }

    /** Una "foto" del arreglo en un momento del ordenamiento. */
    static class Paso {
        final Jugador[] orden;
        final int a, b; // posiciones resaltadas (-1 = ninguna)
        final String nota;
        final int comparaciones, intercambios;
        final boolean fin;

        Paso(Jugador[] orden, int a, int b, String nota, int c, int s, boolean fin) {
            this.orden = orden.clone();
            this.a = a;
            this.b = b;
            this.nota = nota;
            this.comparaciones = c;
            this.intercambios = s;
            this.fin = fin;
        }
    }

    /** Mismo Shell Sort, pero guardando cada comparacion e intercambio para animarlo. */
    static List<Paso> generarPasos(Jugador[] original) {
        List<Paso> pasos = new ArrayList<>();
        Jugador[] v = original.clone();
        int n = v.length, comp = 0, inter = 0;

        for (int gap = n / 2; gap > 0; gap /= 2) {
            pasos.add(new Paso(v, -1, -1, "Nueva ronda: gap = " + gap, comp, inter, false));
            for (int i = gap; i < n; i++) {
                int j = i;
                while (j >= gap) {
                    comp++;
                    pasos.add(new Paso(v, j, j - gap,
                        "gap " + gap + ": comparo " + v[j - gap].nombre + " (" + v[j - gap].altura
                            + ") con " + v[j].nombre + " (" + v[j].altura + ")",
                        comp, inter, false));
                    if (v[j - gap].altura > v[j].altura) {
                        Jugador t = v[j];
                        v[j] = v[j - gap];
                        v[j - gap] = t;
                        inter++;
                        pasos.add(new Paso(v, j, j - gap,
                            "Intercambio: " + v[j - gap].nombre + " sube a la posición " + (j - gap + 1),
                            comp, inter, false));
                        j -= gap;
                    } else {
                        break;
                    }
                }
            }
        }
        pasos.add(new Paso(v, -1, -1, "¡Listo! Ordenados de menor a mayor altura", comp, inter, true));
        return pasos;
    }

    // ====================================================================
    //  PANEL QUE DIBUJA LAS BARRAS
    // ====================================================================
    static class PanelBarras extends JPanel {
        Jugador[] orden = new Jugador[0];
        int a = -1, b = -1;

        PanelBarras() {
            setPreferredSize(new Dimension(720, 290));
            setBackground(Color.WHITE);
        }

        void mostrar(Jugador[] orden, int a, int b) {
            this.orden = orden;
            this.a = a;
            this.b = b;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int n = orden.length;
            if (n == 0) return;
            int margen = 10, hueco = 8;
            int ancho = (getWidth() - 2 * margen - (n - 1) * hueco) / n;
            int base = getHeight() - 24;
            for (int k = 0; k < n; k++) {
                Jugador p = orden[k];
                int alto = (int) ((p.altura - 140) * 4.2);
                int x = margen + k * (ancho + hueco);
                g.setColor(p.color);
                g.fillRoundRect(x, base - alto, ancho, alto, 8, 8);
                if (k == a || k == b) {
                    g.setColor(NARANJA);
                    g.setStroke(new BasicStroke(3f));
                    g.drawRoundRect(x - 2, base - alto - 2, ancho + 4, alto + 4, 10, 10);
                }
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 11));
                g.drawString(String.valueOf(p.altura), x + 4, base - alto + 15);
                g.setColor(Color.DARK_GRAY);
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g.drawString(p.nombre, x, base + 16);
            }
        }
    }

    // ====================================================================
    //  ESTADO Y COMPONENTES
    // ====================================================================
    private final CardLayout cartas = new CardLayout();
    private final JPanel raiz = new JPanel(cartas);
    private final Random azar = new Random();

    // intro
    private int lineaIntro = 0;
    private Timer escritura;
    private String textoCompleto = "";
    private int letras = 0;
    private final JPanel avatar = new JPanel();
    private final JLabel nombreHabla = new JLabel();
    private final JTextArea texto = new JTextArea();
    private final JButton btnSiguiente = new JButton("Siguiente >");

    // ordenamiento
    private final PanelBarras barras = new PanelBarras();
    private final JLabel info = new JLabel(" ");
    private final JLabel stats = new JLabel(" ");
    private final JButton btnMezclar = new JButton("Mezclar");
    private final JButton btnPaso = new JButton("Paso");
    private final JButton btnAuto = new JButton("Auto");
    private Timer autoTimer;
    private Jugador[] actual;
    private List<Paso> pasos;
    private int indicePaso;

    // rotacion y partido
    private final JPanel post = new JPanel();
    private final JPanel cancha = new JPanel(new GridLayout(2, 3, 6, 6));
    private Jugador[] zonas = new Jugador[7]; // posiciones 1..6
    private Jugador[] ordenados;
    private Jugador[] equipoA, equipoB;
    private int puntosA, puntosB;
    private final JLabel equipos = new JLabel();
    private final JLabel marcador = new JLabel();
    private final JLabel bitacora = new JLabel(" ");
    private final JButton btnSaque = new JButton("¡Saque!");

    public HaikyuuShellSort() {
        super("Karasuno · Shell Sort");
        raiz.add(crearIntro(), "intro");
        raiz.add(new JScrollPane(crearJuego()), "juego");
        setContentPane(raiz);
        setSize(800, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        decir();
    }

    // ====================================================================
    //  INTRO
    // ====================================================================
    private JPanel crearIntro() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        p.setBackground(new Color(0xFFF7ED));

        JLabel titulo = new JLabel("Karasuno · Shell Sort");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        p.add(titulo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.setOpaque(false);

        avatar.setPreferredSize(new Dimension(56, 56));
        nombreHabla.setFont(new Font("SansSerif", Font.BOLD, 18));
        JPanel quien = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        quien.setOpaque(false);
        quien.add(avatar);
        quien.add(nombreHabla);
        centro.add(quien, BorderLayout.NORTH);

        texto.setEditable(false);
        texto.setLineWrap(true);
        texto.setWrapStyleWord(true);
        texto.setOpaque(false);
        texto.setFont(new Font("SansSerif", Font.PLAIN, 20));
        centro.add(texto, BorderLayout.CENTER);
        p.add(centro, BorderLayout.CENTER);

        JButton saltar = new JButton("Saltar intro");
        btnSiguiente.addActionListener(e -> siguienteLinea());
        saltar.addActionListener(e -> iniciarJuego());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.setOpaque(false);
        botones.add(btnSiguiente);
        botones.add(saltar);
        p.add(botones, BorderLayout.SOUTH);
        return p;
    }

    /** Muestra la linea actual con efecto de maquina de escribir. */
    private void decir() {
        String quien = INTRO[lineaIntro][0];
        textoCompleto = INTRO[lineaIntro][1];
        letras = 0;
        texto.setText("");
        nombreHabla.setText(quien);
        avatar.setBackground(colorDe(quien));
        avatar.removeAll();
        JLabel inicial = new JLabel(quien.substring(0, 1));
        inicial.setForeground(Color.WHITE);
        inicial.setFont(new Font("SansSerif", Font.BOLD, 24));
        avatar.setLayout(new GridBagLayout());
        avatar.add(inicial);
        avatar.revalidate();
        avatar.repaint();

        if (escritura != null) escritura.stop();
        escritura = new Timer(18, e -> {
            letras++;
            texto.setText(textoCompleto.substring(0, letras));
            if (letras >= textoCompleto.length()) escritura.stop();
        });
        escritura.start();
        btnSiguiente.setText(lineaIntro == INTRO.length - 1 ? "Jugar >" : "Siguiente >");
    }

    private void siguienteLinea() {
        if (escritura != null && escritura.isRunning()) { // primer clic: completa el texto
            escritura.stop();
            texto.setText(textoCompleto);
            return;
        }
        if (lineaIntro < INTRO.length - 1) {
            lineaIntro++;
            decir();
        } else {
            iniciarJuego();
        }
    }

    private static Color colorDe(String nombre) {
        for (Jugador j : PLANTEL) if (j.nombre.equals(nombre)) return j.color;
        return COLOR_NARRADOR;
    }

    private void iniciarJuego() {
        if (escritura != null) escritura.stop();
        cartas.show(raiz, "juego");
        reiniciar();
    }

    // ====================================================================
    //  JUEGO
    // ====================================================================
    private JPanel tarjeta(String titulo) {
        JPanel t = new JPanel(new BorderLayout(6, 6));
        t.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder(titulo), BorderFactory.createEmptyBorder(6, 8, 8, 8)));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        return t;
    }

    private JPanel crearJuego() {
        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        // --- 1. ordenamiento ---
        JPanel t1 = tarjeta("1. Ordenar a los jugadores por altura (cm)");
        t1.add(barras, BorderLayout.CENTER);
        info.setFont(new Font("SansSerif", Font.BOLD, 14));
        JPanel abajo = new JPanel(new GridLayout(2, 1));
        abajo.add(info);
        abajo.add(stats);
        JPanel sur = new JPanel(new BorderLayout());
        sur.add(abajo, BorderLayout.NORTH);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(btnMezclar);
        botones.add(btnPaso);
        botones.add(btnAuto);
        sur.add(botones, BorderLayout.SOUTH);
        t1.add(sur, BorderLayout.SOUTH);
        contenido.add(t1);

        btnMezclar.addActionListener(e -> reiniciar());
        btnPaso.addActionListener(e -> avanzar());
        autoTimer = new Timer(380, e -> avanzar());
        btnAuto.addActionListener(e -> {
            if (autoTimer.isRunning()) detenerAuto();
            else { btnAuto.setText("Pausa"); autoTimer.start(); }
        });

        // --- 2. rotacion ---
        post.setLayout(new BoxLayout(post, BoxLayout.Y_AXIS));
        post.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel t2 = tarjeta("2. Rotación inicial: los 6 más altos (3 más altos en la red)");
        JLabel red = new JLabel("— RED —", SwingConstants.CENTER);
        t2.add(red, BorderLayout.NORTH);
        t2.add(cancha, BorderLayout.CENTER);
        JButton rotar = new JButton("Rotar");
        rotar.addActionListener(e -> rotar());
        JPanel pr = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pr.add(rotar);
        t2.add(pr, BorderLayout.SOUTH);
        post.add(t2);

        // --- 3. partido de la banca ---
        JPanel t3 = tarjeta("3. Banca: mini partido 2 vs 2 (a 5 puntos)");
        marcador.setFont(new Font("SansSerif", Font.BOLD, 26));
        JPanel centro = new JPanel(new GridLayout(3, 1));
        centro.add(equipos);
        centro.add(marcador);
        centro.add(bitacora);
        t3.add(centro, BorderLayout.CENTER);
        JButton reiniciarPartido = new JButton("Reiniciar partido");
        btnSaque.addActionListener(e -> saque());
        reiniciarPartido.addActionListener(e -> prepararPartido());
        JPanel pb = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pb.add(btnSaque);
        pb.add(reiniciarPartido);
        t3.add(pb, BorderLayout.SOUTH);
        post.add(t3);

        post.setVisible(false);
        contenido.add(post);
        return contenido;
    }

    private void detenerAuto() {
        autoTimer.stop();
        btnAuto.setText("Auto");
    }

    private void reiniciar() {
        detenerAuto();
        List<Jugador> lista = new ArrayList<>(List.of(PLANTEL));
        java.util.Collections.shuffle(lista, azar);
        actual = lista.toArray(new Jugador[0]);
        pasos = generarPasos(actual);
        indicePaso = 0;
        barras.mostrar(actual, -1, -1);
        info.setText("Pulsa Paso o Auto para empezar");
        stats.setText(" ");
        post.setVisible(false);
        btnPaso.setEnabled(true);
        btnAuto.setEnabled(true);
    }

    private void avanzar() {
        if (indicePaso >= pasos.size()) return;
        Paso p = pasos.get(indicePaso++);
        barras.mostrar(p.orden, p.a, p.b);
        info.setText(p.nota);
        stats.setText("Comparaciones: " + p.comparaciones + "  ·  Intercambios: " + p.intercambios);
        if (p.fin) {
            detenerAuto();
            btnPaso.setEnabled(false);
            btnAuto.setEnabled(false);
            terminarOrdenamiento(p.orden);
        }
    }

    // ====================================================================
    //  ROTACION Y MINI PARTIDO
    // ====================================================================
    private void terminarOrdenamiento(Jugador[] orden) {
        ordenados = orden;
        // De mayor a menor: los 3 primeros a la red (zonas 3, 4, 2), los otros 3 al fondo (6, 5, 1)
        zonas[3] = orden[9];
        zonas[4] = orden[8];
        zonas[2] = orden[7];
        zonas[6] = orden[6];
        zonas[5] = orden[5];
        zonas[1] = orden[4];
        dibujarCancha();
        prepararPartido();
        post.setVisible(true);
        post.revalidate();
    }

    private void dibujarCancha() {
        cancha.removeAll();
        for (int z : CUADRICULA) {
            Jugador j = zonas[z];
            JLabel c = new JLabel("<html><center><small>Zona " + z + "</small><br><b>" + j.nombre
                + "</b><br>" + j.altura + " cm</center></html>", SwingConstants.CENTER);
            c.setBorder(new LineBorder(j.color, 2, true));
            c.setPreferredSize(new Dimension(120, 64));
            cancha.add(c);
        }
        cancha.revalidate();
        cancha.repaint();
    }

    /** Rotacion en sentido del reloj: 4 -> 3 -> 2 -> 1 -> 6 -> 5 -> 4 */
    private void rotar() {
        Jugador[] nuevo = new Jugador[7];
        for (int k = 0; k < 6; k++) nuevo[ANILLO[(k + 1) % 6]] = zonas[ANILLO[k]];
        zonas = nuevo;
        dibujarCancha();
    }

    private void prepararPartido() {
        // Los 4 mas bajos quedan en banca; equipos balanceados: (menor + mayor) vs (los dos del medio)
        equipoA = new Jugador[]{ordenados[0], ordenados[3]};
        equipoB = new Jugador[]{ordenados[1], ordenados[2]};
        puntosA = puntosB = 0;
        btnSaque.setEnabled(true);
        double ha = equipoA[0].altura + equipoA[1].altura;
        double hb = equipoB[0].altura + equipoB[1].altura;
        equipos.setText("<html><b>Equipo A:</b> " + equipoA[0].nombre + " y " + equipoA[1].nombre
            + " (" + String.format("%.1f", ha) + " cm)<br><b>Equipo B:</b> " + equipoB[0].nombre
            + " y " + equipoB[1].nombre + " (" + String.format("%.1f", hb) + " cm)</html>");
        bitacora.setText(" ");
        actualizarMarcador();
    }

    private void actualizarMarcador() {
        marcador.setText("A " + puntosA + " – " + puntosB + " B");
    }

    private void saque() {
        double ha = equipoA[0].altura + equipoA[1].altura;
        double hb = equipoB[0].altura + equipoB[1].altura;
        boolean ganaA = azar.nextDouble() < ha / (ha + hb); // a mayor altura, mas probabilidad
        if (ganaA) puntosA++; else puntosB++;
        Jugador[] g = ganaA ? equipoA : equipoB;
        bitacora.setText("Punto para " + (ganaA ? "A" : "B") + ": remate de " + g[azar.nextInt(2)].nombre);
        actualizarMarcador();
        if (puntosA == 5 || puntosB == 5) {
            btnSaque.setEnabled(false);
            bitacora.setText("¡Gana el equipo " + (puntosA == 5 ? "A" : "B") + "! Fin del partido de la banca.");
        }
    }

    // ====================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorada) { }
            new HaikyuuShellSort().setVisible(true);
        });
    }
}
