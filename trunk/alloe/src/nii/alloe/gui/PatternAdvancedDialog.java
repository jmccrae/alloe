/*
 * PatternAdvancedDialog.java
 *
 * Created on January 26, 2008, 6:38 AM
 */

package nii.alloe.gui;
import nii.alloe.corpus.pattern.*;
import nii.alloe.classify.*;
import javax.swing.*;
import java.io.*;

/**
 *
 * @author  john
 */
public class PatternAdvancedDialog extends javax.swing.JDialog {
    private PatternSet patternSet;
    private JFileChooser fileChooser;
    
    /** Creates new form PatternAdvancedDialog */
    public PatternAdvancedDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        fileChooser = new JFileChooser();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        ignoreReflexives = new javax.swing.JCheckBox();
        generateAll = new javax.swing.JRadioButton();
        baseOnly = new javax.swing.JRadioButton();
        useBase = new javax.swing.JRadioButton();
        basePatternLabel = new javax.swing.JLabel();
        openBasePatterns = new javax.swing.JButton();
        limitIteartionsCheck = new javax.swing.JCheckBox();
        maxIterations = new javax.swing.JSpinner();
        jPanel2 = new javax.swing.JPanel();
        noFilter = new javax.swing.JRadioButton();
        scoreFilter = new javax.swing.JRadioButton();
        scoreLabel = new javax.swing.JLabel();
        score = new javax.swing.JSpinner();
        sizeFilter = new javax.swing.JRadioButton();
        sizeLabel = new javax.swing.JLabel();
        size = new javax.swing.JSpinner();
        applySupervisedFiltering = new javax.swing.JButton();
        supervisedLabel = new javax.swing.JLabel();
        supervizedSize = new javax.swing.JLabel();
        supervisedSize = new javax.swing.JSpinner();
        filterLabel = new javax.swing.JLabel();
        filter = new javax.swing.JComboBox();
        alphaSlider = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Pattern Builder Advanced Settings");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Generation"));

        ignoreReflexives.setText("Ignore Reflexives");
        ignoreReflexives.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ignoreReflexives.setEnabled(false);
        ignoreReflexives.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreReflexivesActionPerformed(evt);
            }
        });

        buttonGroup1.add(generateAll);
        generateAll.setSelected(true);
        generateAll.setText("Generate All");
        generateAll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        generateAll.setEnabled(false);
        generateAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateAllActionPerformed(evt);
            }
        });

        buttonGroup1.add(baseOnly);
        baseOnly.setText("Generate Base Patterns Only");
        baseOnly.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        baseOnly.setEnabled(false);
        baseOnly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                baseOnlyActionPerformed(evt);
            }
        });

        buttonGroup1.add(useBase);
        useBase.setText("Use Base Pattern Set");
        useBase.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useBase.setEnabled(false);
        useBase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useBaseActionPerformed(evt);
            }
        });

        basePatternLabel.setText("Base Pattern Set:");
        basePatternLabel.setEnabled(false);

        openBasePatterns.setText("Open");
        openBasePatterns.setEnabled(false);
        openBasePatterns.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openBasePatternsActionPerformed(evt);
            }
        });

        limitIteartionsCheck.setText("Limit Iterations");
        limitIteartionsCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        limitIteartionsCheck.setEnabled(false);
        limitIteartionsCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limitIteartionsCheckActionPerformed(evt);
            }
        });

        maxIterations.setModel(new SpinnerNumberModel(0,0,Integer.MAX_VALUE-1,1));
        maxIterations.setEnabled(false);
        maxIterations.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxIterationsStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ignoreReflexives)
                            .addComponent(generateAll)
                            .addComponent(baseOnly)
                            .addComponent(useBase)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addComponent(basePatternLabel))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(limitIteartionsCheck)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 141, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(maxIterations)
                            .addComponent(openBasePatterns, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(ignoreReflexives)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(generateAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(baseOnly)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useBase)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openBasePatterns)
                    .addComponent(basePatternLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(limitIteartionsCheck)
                    .addComponent(maxIterations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Filtering"));

        buttonGroup2.add(noFilter);
        noFilter.setSelected(true);
        noFilter.setText("No Filter");
        noFilter.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noFilter.setEnabled(false);
        noFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noFilterActionPerformed(evt);
            }
        });

        buttonGroup2.add(scoreFilter);
        scoreFilter.setText("Filter by score");
        scoreFilter.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scoreFilter.setEnabled(false);
        scoreFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scoreFilterActionPerformed(evt);
            }
        });

        scoreLabel.setText("Score greater than:");
        scoreLabel.setEnabled(false);

        score.setModel(new SpinnerNumberModel((double)0.0,(double)0.0,(double)1000000,(double)0.0005));
        score.setEnabled(false);
        score.setEditor(new JSpinner.NumberEditor(score,"0.0000"));
        score.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                scoreStateChanged(evt);
            }
        });

        buttonGroup2.add(sizeFilter);
        sizeFilter.setText("Top Patterns");
        sizeFilter.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sizeFilter.setEnabled(false);
        sizeFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sizeFilterActionPerformed(evt);
            }
        });

        sizeLabel.setText("Pattern Set Size:");
        sizeLabel.setEnabled(false);

        size.setModel(new SpinnerNumberModel(0,0,Integer.MAX_VALUE,1));
        size.setEnabled(false);
        size.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sizeStateChanged(evt);
            }
        });

        applySupervisedFiltering.setText("Apply");
        applySupervisedFiltering.setEnabled(false);
        applySupervisedFiltering.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applySupervisedFilteringActionPerformed(evt);
            }
        });

        supervisedLabel.setText("Post-generation supervised filtering");
        supervisedLabel.setEnabled(false);

        supervizedSize.setText("Pattern Set Size:");
        supervizedSize.setEnabled(false);

        supervisedSize.setModel(new SpinnerNumberModel(0,0,Integer.MAX_VALUE,1));
        supervisedSize.setEnabled(false);

        filterLabel.setText("Filter:");
        filterLabel.setEnabled(false);

        filter.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        filter.setEnabled(false);

        alphaSlider.setMaximum(20);
        alphaSlider.setMinimum(-20);
        alphaSlider.setValue(0);
        alphaSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                alphaSliderStateChanged(evt);
            }
        });

        jLabel1.setText("Alpha Value (High Recall <-> High Precision)");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(alphaSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                    .addComponent(noFilter)
                    .addComponent(scoreFilter)
                    .addComponent(sizeFilter)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(scoreLabel)
                            .addComponent(sizeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 94, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(size)
                            .addComponent(score, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(filterLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filter, 0, 217, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(applySupervisedFiltering))
                    .addComponent(supervisedLabel)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(supervizedSize)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 132, Short.MAX_VALUE)
                        .addComponent(supervisedSize, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(alphaSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noFilter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scoreFilter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(score, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scoreLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(sizeFilter)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sizeLabel))
                    .addComponent(size, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(supervisedLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(supervizedSize))
                    .addComponent(supervisedSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(filterLabel)
                        .addComponent(filter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(applySupervisedFiltering)))
                .addContainerGap())
        );

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(closeButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void applySupervisedFilteringActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applySupervisedFilteringActionPerformed
        if(sizeFilter.isSelected()) {
            patternSet.limitToTop((Integer)size.getValue());
        } else if(scoreFilter.isSelected()) {
            patternSet.limitToScore((Double)score.getValue());
        } else {
            JOptionPane.showMessageDialog(this, "Stop being so lazy and implement this stuff!");
        }
    }//GEN-LAST:event_applySupervisedFilteringActionPerformed
    
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed
    
    private void sizeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sizeStateChanged
        if(patternBuilder != null)
            patternBuilder.setMaxPatterns(((Integer)size.getValue()).intValue());
    }//GEN-LAST:event_sizeStateChanged
    
    private void sizeFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sizeFilterActionPerformed
        scoreLabel.setEnabled(false);
        score.setEnabled(false);
        sizeLabel.setEnabled(true);
        size.setEnabled(true);
        if(patternBuilder != null) {
            patternBuilder.setMaxPatterns(((Integer)size.getValue()).intValue());
            patternBuilder.setScoreFilter(0);
        }
    }//GEN-LAST:event_sizeFilterActionPerformed
    
    private void scoreStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_scoreStateChanged
        patternBuilder.setScoreFilter(((Double)score.getValue()).doubleValue());
    }//GEN-LAST:event_scoreStateChanged
    
    private void scoreFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scoreFilterActionPerformed
        scoreLabel.setEnabled(true);
        score.setEnabled(true);
        sizeLabel.setEnabled(false);
        size.setEnabled(false);
        if(patternBuilder != null) {
            patternBuilder.setMaxPatterns(0);
            patternBuilder.setScoreFilter(((Double)score.getValue()).doubleValue());
        }
    }//GEN-LAST:event_scoreFilterActionPerformed
    
    private void noFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noFilterActionPerformed
        scoreLabel.setEnabled(false);
        score.setEnabled(false);
        sizeLabel.setEnabled(false);
        size.setEnabled(false);
        if(patternBuilder != null) {
            patternBuilder.setMaxPatterns(0);
            patternBuilder.setScoreFilter(0);
        }
    }//GEN-LAST:event_noFilterActionPerformed
    
    private void maxIterationsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxIterationsStateChanged
        patternBuilder.setMaxIterations(((Integer)maxIterations.getValue()).intValue());
    }//GEN-LAST:event_maxIterationsStateChanged
    
    private void limitIteartionsCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limitIteartionsCheckActionPerformed
        maxIterations.setEnabled(limitIteartionsCheck.isSelected());
        if(!limitIteartionsCheck.isSelected())
            patternBuilder.unsetMaxIterations();
        else
            patternBuilder.setMaxIterations(((Integer)maxIterations.getValue()).intValue());
    }//GEN-LAST:event_limitIteartionsCheckActionPerformed
    
    private void openBasePatternsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openBasePatternsActionPerformed
        if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileChooser.getSelectedFile()));
                Object o = ois.readObject();
                if(!(o instanceof PatternSet)) {
                    JOptionPane.showMessageDialog(this, "Invalid Format", "Could not open pattern set", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                patternSet = (PatternSet)o;
                patternBuilder.setBasePatterns(patternSet);
                basePatternLabel.setText("Base Pattern Set: " + fileChooser.getSelectedFile().getName());
            } catch(IOException x) {
                JOptionPane.showMessageDialog(this, x.getMessage(), "Could not open pattern set", JOptionPane.ERROR_MESSAGE);
            } catch(ClassNotFoundException x) {
                JOptionPane.showMessageDialog(this, x.getMessage(), "Could not open pattern set", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_openBasePatternsActionPerformed
    
    private void useBaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useBaseActionPerformed
        basePatternLabel.setEnabled(true);
        openBasePatterns.setEnabled(true);
        patternBuilder.setGenerateBaseOnly(false);
        patternBuilder.setBasePatterns(patternSet);
    }//GEN-LAST:event_useBaseActionPerformed
    
    private void baseOnlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_baseOnlyActionPerformed
        basePatternLabel.setEnabled(false);
        openBasePatterns.setEnabled(false);
        patternBuilder.setGenerateBaseOnly(true);
        patternBuilder.setBasePatterns(null);
    }//GEN-LAST:event_baseOnlyActionPerformed
    
    private void generateAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateAllActionPerformed
        basePatternLabel.setEnabled(false);
        openBasePatterns.setEnabled(false);
        patternBuilder.setGenerateBaseOnly(false);
        patternBuilder.setBasePatterns(null);
    }//GEN-LAST:event_generateAllActionPerformed
    
    private void ignoreReflexivesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoreReflexivesActionPerformed
        patternBuilder.setIgnoreReflexives(ignoreReflexives.isSelected());
    }//GEN-LAST:event_ignoreReflexivesActionPerformed

    private void alphaSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_alphaSliderStateChanged
        if(getPatternBuilder() instanceof PatternSetBuilder) {
            ((PatternSetBuilder)getPatternBuilder()).setAlpha((double)alphaSlider.getValue());
            
        } else {
            getPatternBuilder().setMetricAlpha((double)alphaSlider.getValue());
        }
}//GEN-LAST:event_alphaSliderStateChanged
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PatternAdvancedDialog(new javax.swing.JFrame(), true).setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider alphaSlider;
    private javax.swing.JButton applySupervisedFiltering;
    private javax.swing.JRadioButton baseOnly;
    private javax.swing.JLabel basePatternLabel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton closeButton;
    private javax.swing.JComboBox filter;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JRadioButton generateAll;
    private javax.swing.JCheckBox ignoreReflexives;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JCheckBox limitIteartionsCheck;
    private javax.swing.JSpinner maxIterations;
    private javax.swing.JRadioButton noFilter;
    private javax.swing.JButton openBasePatterns;
    private javax.swing.JSpinner score;
    private javax.swing.JRadioButton scoreFilter;
    private javax.swing.JLabel scoreLabel;
    private javax.swing.JSpinner size;
    private javax.swing.JRadioButton sizeFilter;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JLabel supervisedLabel;
    private javax.swing.JSpinner supervisedSize;
    private javax.swing.JLabel supervizedSize;
    private javax.swing.JRadioButton useBase;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Holds value of property patternBuilder.
     */
    private PatternBuilder patternBuilder;
    
    /**
     * Getter for property patternBuilder.
     * @return Value of property patternBuilder.
     */
    public PatternBuilder getPatternBuilder() {
        return this.patternBuilder;
    }
    
    /**
     * Setter for property patternBuilder.
     * @param patternBuilder New value of property patternBuilder.
     */
    public void setPatternBuilder(PatternBuilder patternBuilder) {
        this.patternBuilder = patternBuilder;
        baseOnly.setEnabled(patternBuilder != null);
        basePatternLabel.setEnabled(patternBuilder != null && useBase.isSelected());
        generateAll.setEnabled(patternBuilder != null);
        ignoreReflexives.setEnabled(patternBuilder != null);
        noFilter.setEnabled((patternBuilder != null || patternSet != null));
        openBasePatterns.setEnabled(patternBuilder != null && useBase.isSelected());
        score.setEnabled((patternBuilder != null || patternSet != null) && scoreFilter.isSelected());
        scoreFilter.setEnabled((patternBuilder != null || patternSet != null));
        scoreLabel.setEnabled((patternBuilder != null || patternSet != null) && scoreFilter.isSelected());
        size.setEnabled((patternBuilder != null || patternSet != null) && sizeFilter.isSelected());
        sizeFilter.setEnabled((patternBuilder != null || patternSet != null));
        sizeLabel.setEnabled((patternBuilder != null || patternSet != null) && sizeFilter.isSelected());
        useBase.setEnabled(patternBuilder != null);
        limitIteartionsCheck.setEnabled(patternBuilder != null);
        maxIterations.setEnabled(patternBuilder != null && limitIteartionsCheck.isSelected());
    }
    
    /**
     * Holds value of property basePatternSet.
     */
    private PatternSet basePatternSet;
    
    /**
     * Getter for property basePatternSet.
     * @return Value of property basePatternSet.
     */
    public PatternSet getBasePatternSet() {
        return this.basePatternSet;
    }
    
    /**
     * Setter for property basePatternSet.
     * @param basePatternSet New value of property basePatternSet.
     */
    public void setBasePatternSet(PatternSet basePatternSet) {
        this.basePatternSet = basePatternSet;
    }
    
    public void setPatternSet(PatternSet patternSet) {
        this.patternSet = patternSet;
        noFilter.setEnabled((patternBuilder != null || patternSet != null));
        scoreFilter.setEnabled((patternBuilder != null || patternSet != null));
        scoreLabel.setEnabled((patternBuilder != null || patternSet != null) && scoreFilter.isSelected());
        score.setEnabled((patternBuilder != null || patternSet != null) && scoreFilter.isSelected());
        sizeFilter.setEnabled((patternBuilder != null || patternSet != null));
        sizeLabel.setEnabled((patternBuilder != null || patternSet != null) && sizeFilter.isSelected());
        size.setEnabled((patternBuilder != null || patternSet != null) && sizeFilter.isSelected());
        applySupervisedFiltering.setEnabled(patternSet != null);
    }
    
    /**
     * Holds value of property dataSet.
     */
    private DataSet dataSet;
    
    /**
     * Getter for property dataSet.
     * @return Value of property dataSet.
     */
    public DataSet getDataSet() {
        return this.dataSet;
    }
    
    /**
     * Setter for property dataSet.
     * @param dataSet New value of property dataSet.
     */
    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }
    /**
     * Getter for property patternSetSize.
     * @return Value of property patternSetSize.
     */
    public int getPatternSetSize() {
        return ((Integer)size.getValue()).intValue();
    }

    /**
     * Setter for property patternSetSize.
     * @param patternSetSize New value of property patternSetSize.
     */
    public void setPatternSetSize(int patternSetSize) {
        size.setValue((Integer)patternSetSize);
    }
    
}
