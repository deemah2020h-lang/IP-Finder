package ip_finder; // تعريف الحزمة (package) اللي يحتويها البرنامج

// استيراد المكتبات اللي يحتاجها البرنامج
import java.awt.*; // لعناصر الواجهة الرسومية (ألوان، أزرار، خطوط، الخ)
import java.awt.datatransfer.StringSelection; // لنسخ النص إلى الحافظة (clipboard)
import java.awt.event.*; // للتعامل مع الأحداث مثل الضغط على الأزرار
import java.io.IOException; // لمعالجة الأخطاء عند الاتصال بالشبكة
import java.net.*; // لعناوين IP و URIs
import java.net.http.*; // لاستخدام HttpClient (للاتصال بالإنترنت)
import java.time.Duration; // لتحديد مهلة الاتصال
import java.util.Locale; //تحديد لغة أو منطقة معينة (مثل EN أو AR)
import java.util.regex.Matcher;//إنشاء نمط بحث في النصوص
import java.util.regex.Pattern; //تنفيذ عملية البحث باستخدام النمط
import javax.swing.*; // لعناصر الواجهة مثل JFrame و JButton و JLabel
import java.nio.charset.StandardCharsets;//قراءة نصوص عربية أو لغات أخرى بدون مشاكل

public class Main extends JFrame implements ActionListener { // إنشاء نافذة GUI وتنفيذ ActionListener لتتعامل مع الضغطات

    // تعريف العناصر اللي بتستخدم في الواجهة
    JPanel mainPanel;
    JLabel label, labelResult, backgroundLabel;
    JTextField textField;
    JButton buttonFind, btnDetails, btnMap, btnCopy, btnPing, buttonClear;
    DefaultListModel<String> historyModel;
    JList<String> historyList;
    JScrollPane historyScroll;

    static final String HISTORY_FILE = "history.csv"; // ملف لحفظ السجل (حالياً مو مستخدم بشكل فعلي)

    // قائمة بالمواقع المشهورة عشان يكشف المواقع اللي تشبهها ويحذر المستخدم
    private final String[] popularSites = {
            "google.com", "facebook.com", "twitter.com", "youtube.com", "amazon.com",
            "linkedin.com", "instagram.com", "microsoft.com", "apple.com", "paypal.com"
    };

    // إنشاء كائن HttpClient للاتصال بالإنترنت (API)
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // المُنشئ (Constructor)
    public Main() {
        createAndShowGUI(); // ينشئ الواجهة
        loadHistory(); // يحمل سجل المواقع السابقة
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main()); // يشغل البرنامج على واجهة Swing
    }

    private void createAndShowGUI() {
        // إنشاء الطبقات (واحدة للخلفية وواحدة للعناصر فوقها)
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(700, 450));
        setContentPane(layeredPane);

        // إضافة الخلفية المتحركة (GIF)
        try {
            ImageIcon gifIcon = new ImageIcon(this.getClass().getResource("/images/map.gif"));
            backgroundLabel = new JLabel(gifIcon);
        } catch (Exception ex) {
            backgroundLabel = new JLabel();
        }
        backgroundLabel.setBounds(0, 0, 700, 450);
        layeredPane.add(backgroundLabel, Integer.valueOf(0)); // الخلفية في الطبقة 0

        // إضافة لوحة شفافة للعناصر فوق الخلفية
        mainPanel = new JPanel(null);
        mainPanel.setOpaque(false);
        mainPanel.setBounds(0, 0, 700, 450);
        layeredPane.add(mainPanel, Integer.valueOf(1));

        // إنشاء العناصر الأساسية
        label = new JLabel("Enter URL", JLabel.CENTER); // عنوان الحقل
        textField = new JTextField(); // المستخدم يكتب فيه الموقع
        buttonFind = new JButton("Get IP"); // الزر اللي يبحث عن IP
        labelResult = new JLabel("", JLabel.CENTER); // النتيجة تظهر هنا

        // الأزرار الإضافية
        btnDetails = new JButton("Details");
        btnMap = new JButton("Open Map");
        btnCopy = new JButton("Copy IP");
        btnPing = new JButton("Ping");
        buttonClear = new JButton("Clear");

        // قائمة السجل
        historyModel = new DefaultListModel<>();
        historyList = new JList<>(historyModel);
        historyScroll = new JScrollPane(historyList);

        // تحديد أماكن العناصر في النافذة
        label.setBounds(200, 30, 300, 30);
        textField.setBounds(200, 70, 300, 36);
        buttonFind.setBounds(510, 70, 100, 36);

        // خصائص النص اللي يعرض النتيجة
        labelResult.setBounds(200, 120, 410, 30);
        labelResult.setFont(new Font("Arial", Font.PLAIN, 18));
        labelResult.setOpaque(true);
        labelResult.setBackground(new Color(255, 255, 255, 150));
        labelResult.setForeground(Color.BLACK);

        // أماكن الأزرار الإضافية
        btnCopy.setBounds(200, 160, 100, 30);
        btnDetails.setBounds(310, 160, 100, 30);
        btnMap.setBounds(420, 160, 100, 30);
        btnPing.setBounds(530, 160, 100, 30);
        historyScroll.setBounds(200, 210, 410, 130);
        buttonClear.setBounds(200, 350, 410, 30);

        // تنسيق الألوان والخطوط
        label.setFont(new Font("Arial", Font.PLAIN, 20));
        label.setForeground(Color.WHITE);

        textField.setFont(new Font("Arial", Font.PLAIN, 18));
        textField.setForeground(Color.LIGHT_GRAY);
        textField.setBackground(new Color(10, 10, 10));
        textField.setCaretColor(Color.WHITE);

        buttonFind.setFont(new Font("Arial", Font.BOLD, 16));
        buttonFind.setForeground(Color.LIGHT_GRAY);
        buttonFind.setBackground(Color.DARK_GRAY);

        // إضافة كل العناصر للواجهة
        mainPanel.add(label);
        mainPanel.add(textField);
        mainPanel.add(buttonFind);
        mainPanel.add(labelResult);
        mainPanel.add(btnDetails);
        mainPanel.add(btnMap);
        mainPanel.add(btnCopy);
        mainPanel.add(btnPing);
        mainPanel.add(historyScroll);
        mainPanel.add(buttonClear);

        // --- وظائف الأزرار ---

        buttonFind.addActionListener(this); // لما يضغط زر Get IP ينفذ actionPerformed

        buttonClear.addActionListener(e -> historyModel.clear()); // يمسح السجل

        // نسخ IP إلى الحافظة
        btnCopy.addActionListener(e -> {
            StringSelection selection = new StringSelection(labelResult.getText().replaceFirst("^IP:\\s*", ""));
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            JOptionPane.showMessageDialog(this, "IP copied to clipboard.");
        });

        // فتح الموقع على الخريطة باستخدام الإحداثيات
        btnMap.addActionListener(e -> {
            String ip = getIPFromLabelOrSelection();
            if (ip == null) {
                JOptionPane.showMessageDialog(this, "No IP available. Get IP first or select history item.");
                return;
            }
            try {
                GeoInfo g = getGeoInfo(ip);
                if (g != null && g.lat != null && g.lon != null) {
                    String url = String.format(Locale.US, "https://www.google.com/maps/search/?api=1&query=%f,%f", g.lat, g.lon);
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    Desktop.getDesktop().browse(new URI("https://www.google.com/maps/search/?api=1&query=" + URLEncoder.encode(ip, StandardCharsets.UTF_8)));
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Cannot open map: " + ex.getMessage());
            }
        });

        // زر Ping يتحقق إذا العنوان يستجيب
        btnPing.addActionListener(e -> {
            String ipText = labelResult.getText();
            if (ipText.startsWith("IP: ")) {
                String ip = ipText.replaceFirst("^IP:\\s*", "");
                try {
                    InetAddress address = InetAddress.getByName(ip);
                    boolean reachable = address.isReachable(3000);
                    JOptionPane.showMessageDialog(this, ip + " is " + (reachable ? "Reachable" : "Not Reachable"));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ping failed.");
                }
            }
        });

        // زر Details يعرض تفاصيل الموقع أو IP
        btnDetails.addActionListener(e -> {
            String selIp = getIPFromLabelOrSelection();
            if (selIp == null) {
                JOptionPane.showMessageDialog(this, "Please select a history item or get an IP first.");
                return;
            }
            try {
                GeoInfo g = getGeoInfo(selIp);
                if (g == null) {
                    JOptionPane.showMessageDialog(this, "No details found for: " + selIp);
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("IP: ").append(nonNull(g.query)).append("\n");
                sb.append("Country: ").append(nonNull(g.country)).append("\n");
                sb.append("Region: ").append(nonNull(g.regionName)).append("\n");
                sb.append("City: ").append(nonNull(g.city)).append("\n");
                sb.append("ZIP: ").append(nonNull(g.zip)).append("\n");
                sb.append("Lat/Lon: ").append(g.lat != null ? g.lat + "," + g.lon : "N/A").append("\n");
                sb.append("Timezone: ").append(nonNull(g.timezone)).append("\n");
                sb.append("ISP: ").append(nonNull(g.isp)).append("\n");
                sb.append("Org: ").append(nonNull(g.org)).append("\n");
                JOptionPane.showMessageDialog(this, sb.toString(), "Details", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Detail lookup failed: " + ex.getMessage());
            }
        });

        // لما المستخدم يضغط مرتين على عنصر من السجل ينسخ العنوان إلى الحقل
        historyList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    String sel = historyList.getSelectedValue();
                    if (sel != null && sel.contains("→")) {
                        String[] parts = sel.split("→");
                        textField.setText(parts[0].trim());
                        labelResult.setText("IP: " + parts[1].trim());
                    }
                }
            }
        });

        // إعدادات النافذة
        setTitle("IP Finder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(700, 450);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // هذا الجزء ينفذ لما يضغط المستخدم زر "Get IP"
    @Override
    public void actionPerformed(ActionEvent e) {
        String input = textField.getText().trim().toLowerCase();
        if (input.isEmpty()) {
            setLabelMessage("Please enter a valid URL", Color.RED);
            return;
        }

        // يتحقق إذا الموقع يشبه أحد المواقع المشهورة (احتيال)
        boolean suspicious = false;
        for (String site : popularSites) {
            int distance = levenshteinDistance(input, site);
            if (!input.equals(site) && distance <= 2) {
                suspicious = true;
                setLabelMessage("site is similar to " + site + " It might be a scam!", new Color(255, 0, 0));
                break;
            }
        }

        // إذا مو موقع مشبوه يحاول يجيب IP
        if (!suspicious) {
            try {
                InetAddress address = InetAddress.getByName(input);
                String ip = address.getHostAddress();
                setLabelMessage("IP: " + ip, new Color(50, 205, 50));
                historyModel.addElement(input + " → " + ip);
            } catch (UnknownHostException ex) {
                setLabelMessage("Invalid URL or Network is Down", Color.RED);
            }
        }
    }

    // يغيّر النص في النتيجة (اللون والخلفية)
    private void setLabelMessage(String text, Color fg) {
        labelResult.setText(text);
        labelResult.setForeground(fg);
        labelResult.setOpaque(true);
        labelResult.setBackground(new Color(255, 255, 255, 150));
    }

    // يحسب الفرق بين كلمتين (للكشف عن المواقع المتشابهة)
    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) dp[i][j] = j;
                else if (j == 0) dp[i][j] = i;
                else dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1),
                                dp[i - 1][j] + 1),
                        dp[i][j - 1] + 1
                );
            }
        }
        return dp[a.length()][b.length()];
    }

    // تحميل سجل (مبدئياً فقط عنصر فارغ)
    private void loadHistory() {
        historyModel.addElement("");
    }

    // --- دوال المساعدة ---

    // ترجع الـ IP من النتيجة أو من السجل
    private String getIPFromLabelOrSelection() {
        String selected = historyList.getSelectedValue();
        if (selected != null && selected.contains("→")) {
            return selected.split("→")[1].trim();
        }
        String ipText = labelResult.getText();
        if (ipText.startsWith("IP: ")) {
            return ipText.replaceFirst("^IP:\\s*", "").trim();
        }
        return null;
    }

    // كلاس داخلي لتخزين معلومات الموقع الجغرافية
    private static class GeoInfo {
        String status, message, country, regionName, city, zip, timezone, isp, org, query;
        Double lat, lon;
    }

    // يجلب معلومات IP من موقع ip-api
    private GeoInfo getGeoInfo(String ip) throws IOException, InterruptedException {
        String url = "http://ip-api.com/json/" + URLEncoder.encode(ip, StandardCharsets.UTF_8)
                + "?fields=status,message,country,regionName,city,zip,lat,lon,timezone,isp,org,query";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(6))
                .GET()
                .build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        String body = resp.body();
        if (body == null || body.isEmpty()) return null;

        String status = extractString(body, "status");
        if (status == null || !status.equalsIgnoreCase("success")) {
            return null;
        }

        GeoInfo g = new GeoInfo();
        g.status = status;
        g.message = extractString(body, "message");
        g.country = extractString(body, "country");
        g.regionName = extractString(body, "regionName");
        g.city = extractString(body, "city");
        g.zip = extractString(body, "zip");
        g.timezone = extractString(body, "timezone");
        g.isp = extractString(body, "isp");
        g.org = extractString(body, "org");
        g.query = extractString(body, "query");
        g.lat = extractDouble(body, "lat");
        g.lon = extractDouble(body, "lon");
        return g;
    }

    // دوال لاستخراج النصوص أو الأرقام من JSON
    private String extractString(String json, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(json);
        if (m.find()) return m.group(1);
        return null;
    }

    private Double extractDouble(String json, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([-]?[0-9]+(?:\\.[0-9]+)?)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(json);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    // لو النص فارغ يرجع "N/A"
    private String nonNull(String s) {
        return s == null || s.isEmpty() ? "N/A" : s;
    }
}
