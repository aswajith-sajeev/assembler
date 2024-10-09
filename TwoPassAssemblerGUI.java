import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.IOException;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.awt.*;
import java.io.File;

class TwoPassAssemblerGUI extends JFrame {
    private final JTextField inputFileField;
    private final JTextField optabFileField;
    private final JButton assembleBtn;
    private final JTextArea intermediateFileOutput;
    private final JTextArea symbolTableOutput;
    private final JTextArea objectCodeOutput;

    public TwoPassAssemblerGUI() throws IOException {
        setTitle("Two-Pass Assembler");
        setSize(850, 700);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font font = new Font("Segoe UI", Font.PLAIN, 16);
        Font font2 = new Font("Consolas", Font.PLAIN, 14);

        // Main panel with background color
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(245, 245, 245));

        // Input label and field
        JLabel inputFileLabel = new JLabel("Input File:");
        inputFileLabel.setFont(font);
        inputFileField = new JTextField(30);
        inputFileField.setFont(font);
        JButton browseBtn = createButton("Browse");
        browseBtn.addActionListener(e -> browseFile(inputFileField));

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        inputPanel.setBackground(new Color(245, 245, 245));
        inputPanel.add(inputFileLabel, BorderLayout.WEST);
        inputPanel.add(inputFileField, BorderLayout.CENTER);
        inputPanel.add(browseBtn, BorderLayout.EAST);

        // Optab label and field
        JLabel optabFileLabel = new JLabel("Optab File:");
        optabFileLabel.setFont(font);
        optabFileField = new JTextField(30);
        optabFileField.setFont(font);
        JButton optabBrowseBtn = createButton("Browse");
        optabBrowseBtn.addActionListener(e -> browseFile(optabFileField));

        // Optab panel
        JPanel optabPanel = new JPanel(new BorderLayout(10, 0));
        optabPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        optabPanel.setBackground(new Color(245, 245, 245));
        optabPanel.add(optabFileLabel, BorderLayout.WEST);
        optabPanel.add(optabFileField, BorderLayout.CENTER);
        optabPanel.add(optabBrowseBtn, BorderLayout.EAST);

        // Assemble button with custom style
        assembleBtn = createButton("Assemble");
        assembleBtn.addActionListener(e -> runAssembler());

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.add(assembleBtn);

        // Text areas with custom borders
        intermediateFileOutput = createTextArea("Intermediate File Output", font2);
        symbolTableOutput = createTextArea("Symbol Table Output", font2);
        objectCodeOutput = createTextArea("Object Code Output", font2);

        // Panel for text areas
        JPanel textAreaPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        textAreaPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        textAreaPanel.setBackground(new Color(245, 245, 245));
        textAreaPanel.add(new JScrollPane(intermediateFileOutput));
        textAreaPanel.add(new JScrollPane(symbolTableOutput));

        // Bottom panel for object code
        JPanel bottomTextAreaPanel = new JPanel(new BorderLayout());
        bottomTextAreaPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        bottomTextAreaPanel.setBackground(new Color(245, 245, 245));
        bottomTextAreaPanel.add(new JScrollPane(objectCodeOutput), BorderLayout.CENTER);

        mainPanel.add(inputPanel);
        mainPanel.add(optabPanel);
        mainPanel.add(buttonPanel);
        mainPanel.add(textAreaPanel);
        mainPanel.add(bottomTextAreaPanel);

        add(mainPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }

    // Utility method to create custom-styled buttons
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(72, 133, 237)); // New color
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 103, 207), 1, true),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setPreferredSize(new Dimension(120, 40));
        return button;
    }

    // Utility method to create custom-styled text areas
    private JTextArea createTextArea(String title, Font font) {
        JTextArea textArea = new JTextArea(10, 30);
        textArea.setFont(font);
        textArea.setEditable(false);
        textArea.setBorder(BorderFactory.createTitledBorder(title));
        textArea.setLineWrap(true);
        return textArea;
    }


    //Browse file
    private void browseFile(JTextField field) {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            field.setText(file.getPath());
        }
    }

    // Run the assembler when the "Assemble" button is clicked
    private void runAssembler() {
        String inputFile = inputFileField.getText();
        String optabFile = optabFileField.getText();

        if (inputFile.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide the input file!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Disable the Assemble button to prevent multiple clicks
        assembleBtn.setEnabled(false);

        // Use a SwingWorker to handle the assembly process in the background
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                TwoPassAssembler assembler = new TwoPassAssembler(inputFile, optabFile);
                try {
                    assembler.loadOptab();   // Load opcode table
                    assembler.passOne();     // Run first pass
                    assembler.passTwo();     // Run second pass

                    // Display outputs from the HashMaps instead of files
                    displayIntermediateCode(assembler.getIntermediate(), assembler.getIntermediateStart());
                    displaySymbolTable(assembler.getSymtab());
                    displayObjectCode(assembler.getObjectCode());

                } catch (IOException e) {
                    JOptionPane.showMessageDialog(TwoPassAssemblerGUI.this, "Error running the assembler: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done() {
                // Re-enable the Assemble button once processing is complete
                assembleBtn.setEnabled(true);
            }
        };

        worker.execute();
    }


    // Method to display intermediate code from the HashMap
    private void displayIntermediateCode(Map<Integer, String> intermediate, Map<Integer, String> intermediateStart) {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<Integer, String> entry : intermediateStart.entrySet()) {
            content.append(String.format("%04X",entry.getKey())).append(entry.getValue()).append("\n");
        }
        for (Map.Entry<Integer, String> entry : intermediate.entrySet()) {
            content.append(String.format("%04X",entry.getKey())).append(entry.getValue()).append("\n");
        }
        intermediateFileOutput.setText(content.toString());  // Display in the text area
    }

    // Method to display symbol table from the HashMap
    private void displaySymbolTable(Map<String, Integer> symtab) {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, Integer> entry : symtab.entrySet()) {
            content.append(entry.getKey()).append("\t").append(String.format("%04X",entry.getValue())).append("\n");
        }
        symbolTableOutput.setText(content.toString());  // Display in the text area
    }

    // Method to display object code from the HashMap
    private void displayObjectCode(Map<Integer, String> objectCode) {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<Integer, String> entry : objectCode.entrySet()) {
            content.append(entry.getValue()).append("\n");
        }
        objectCodeOutput.setText(content.toString());  // Display in the text area
    }

}

class TwoPassAssembler {
    private final String inputFile;
    private final String optabFile;
    private final Map<String, String> optab = new HashMap<>();
    private final Map<String, Integer> symtab = new LinkedHashMap<>();
    private final Map<Integer, String> intermediate = new LinkedHashMap<>();
    private final Map<Integer, String> intermediateStart = new LinkedHashMap<>();
    private final Map<Integer, String> objectCode = new LinkedHashMap<>();
    private int locctr = 0;
    private int start = 0;
    private int length = 0;

    public TwoPassAssembler(String inputFile, String optabFile) {
        this.inputFile = inputFile;
        this.optabFile = optabFile;
    }

    public void loadOptab() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(optabFile));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\\s+");
            optab.put(parts[0], parts[1]);
        }
        reader.close();
    }

    public void passOne() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line = reader.readLine();
        String[] parts = line.split("\\s+");

        if (parts[1].equals("START")) {
            start = Integer.parseInt(parts[2], 16); // Parse start as hexadecimal
            locctr = start;
            intermediateStart.put(locctr, String.format("\t%s\t%s\t%s", parts[0], parts[1], parts[2]));
            line = reader.readLine();
        } else {
            locctr = 0;
        }

        // Process each line
        while (line != null) {
            parts = line.split("\\s+");
            if (parts[1].equals("END")) break;

            // Store intermediate with location counter in hex format
            intermediate.put(locctr, String.format("\t%s\t%s\t%s", parts[0], parts[1], parts[2]));

            // Insert into symbol table if label exists
            if (!parts[0].equals("-")) {
                symtab.put(parts[0], locctr);
            }

            // Update locctr based on the opcode
            if (optab.containsKey(parts[1])) {
                locctr += 3;
            } else if (parts[1].equals("WORD")) {
                locctr += 3;
            } else if (parts[1].equals("BYTE")) {
                locctr += parts[2].length() - 3;
            } else if (parts[1].equals("RESW")) {
                locctr += 3 * Integer.parseInt(parts[2]);
            } else if (parts[1].equals("RESB")) {
                locctr += Integer.parseInt(parts[2]);
            }

            line = reader.readLine();
        }

        // Store final line in intermediate and calculate program length
        intermediate.put(locctr, String.format("\t%s\t%s\t%s", parts[0], parts[1], parts[2]));
        length = locctr - start;

        reader.close();
    }

    public void passTwo() throws IOException {
        String line;
        String[] parts;

        String startLine = intermediateStart.get(start);
        String[] startParts = startLine.trim().split("\\s+");

        // Handle "START" directive
        if (startParts[1].equals("START")) {
            objectCode.put(0, "H^ " + startParts[0] + "^ " + String.format("%06X", start) + "^ " + String.format("%06X", length));
            //line = intermediate.get(start + 1);
        } else {
            objectCode.put(0, "H^ " + " " + "^ 0000^ " + String.format("%06X", length));
        }

        StringBuilder textRecord = new StringBuilder();
        int textStartAddr = 0;
        int textLength = 0;

        for (int loc : intermediate.keySet()) {

            line = intermediate.get(loc);
            parts = line.trim().split("\\s+");
            if (parts.length < 3) continue;

            if (parts[2].equals("END")) break;

            if (textLength == 0) {
                textStartAddr = loc;
                textRecord.append("T^ ").append(String.format("%06X", textStartAddr)).append("^ ");
            }

            // Generate object code for each line
            if (optab.containsKey(parts[1])) {
                String machineCode = optab.get(parts[1]);
                int address = symtab.getOrDefault(parts[2], 0);
                String code = machineCode + String.format("%04X", address);
                textRecord.append(code).append("^ ");
                textLength += code.length() / 2;
            } else if (parts[1].equals("WORD")) {
                String wordCode = String.format("%06X", Integer.parseInt(parts[2]));
                textRecord.append(wordCode).append("^ ");
                textLength += wordCode.length() / 2;
            } else if (parts[1].equals("BYTE")) {
                String byteCode = parts[2].substring(2, parts[2].length() - 1); // Extract value from BYTE literal
                textRecord.append(byteCode).append("^ ");
                textLength += byteCode.length() / 2;
            } else if (parts[1].equals("RESW") || parts[1].equals("RESB")) {
                // If we hit RESW/RESB, flush the current text record and start a new one after reserving memory
                if (textLength > 0) {
                    objectCode.put(textStartAddr, textRecord.toString());
                    textRecord = new StringBuilder();
                    textLength = 0;
                }
                continue; // Do not generate object code for reserved space
            }

            if (textLength >= 30) { // Text records should not exceed 30 bytes (60 hex characters)
                objectCode.put(textStartAddr, textRecord.toString());
                textRecord = new StringBuilder();
                textLength = 0;
            }
        }

        // Write remaining text record if not empty
        if (textLength > 0) {
            objectCode.put(textStartAddr, textRecord.toString());
        }

        // Write End record
        objectCode.put(locctr, "E^ " + String.format("%06X", start));
    }


    public Map<Integer, String> getIntermediate() {
        return intermediate;
    }

    public Map<Integer, String> getIntermediateStart() {
        return intermediateStart;
    }

    public Map<String, Integer> getSymtab() {
        return symtab;
    }

    public Map<Integer, String> getObjectCode() {
        return objectCode;
    }

}

class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TwoPassAssemblerGUI gui;
            try {
                gui = new TwoPassAssemblerGUI();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            gui.setVisible(true);
        });
    }
}