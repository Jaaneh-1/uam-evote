package uam.evote.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.opencsv.CSVWriter;
import uam.evote.model.Election;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportService {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Exporte les résultats d'une élection au format PDF.
     */
    public void exporterPDF(Election election, List<VoteService.ResultatCandidat> resultats,
                            int nbElecteurs, String cheminFichier) throws IOException {

        try (PdfWriter writer = new PdfWriter(cheminFichier);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {

            document.add(new Paragraph("UAM e-Vote — Résultats officiels")
                    .setFontSize(18).setBold());

            document.add(new Paragraph(election.getTitre())
                    .setFontSize(14));

            document.add(new Paragraph(
                    "Période du scrutin : " + election.getDateDebut().format(FORMAT)
                            + " au " + election.getDateFin().format(FORMAT)
            ).setFontSize(10));

            int nbVotes = resultats.stream().mapToInt(VoteService.ResultatCandidat::getNbVotes).sum();
            double taux = nbElecteurs == 0 ? 0 : (nbVotes * 100.0 / nbElecteurs);

            document.add(new Paragraph(
                    String.format("Électeurs éligibles : %d   |   Votes exprimés : %d   |   Participation : %.1f %%",
                            nbElecteurs, nbVotes, taux)
            ).setFontSize(11).setMarginBottom(15));

            // Tableau des résultats
            Table table = new Table(new float[]{3, 2});
            table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

            table.addHeaderCell(new Cell().add(new Paragraph("Candidat").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Votes").setBold())
                    .setTextAlignment(TextAlignment.CENTER));

            for (VoteService.ResultatCandidat resultat : resultats) {
                table.addCell(new Cell().add(new Paragraph(resultat.getCandidat().getNomComplet())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(resultat.getNbVotes())))
                        .setTextAlignment(TextAlignment.CENTER));
            }

            document.add(table);

            if (!resultats.isEmpty() && nbVotes > 0) {
                VoteService.ResultatCandidat premier = resultats.get(0);
                boolean exAequo = resultats.size() > 1
                        && resultats.get(1).getNbVotes() == premier.getNbVotes();

                String texteVainqueur = exAequo
                        ? "Résultat : égalité en tête, aucun vainqueur désigné automatiquement."
                        : "Vainqueur : " + premier.getCandidat().getNomComplet()
                          + " (" + premier.getNbVotes() + " voix)";

                document.add(new Paragraph(texteVainqueur)
                        .setFontSize(13).setBold().setMarginTop(15));
            }

            document.add(new Paragraph(
                    "Document généré automatiquement par UAM e-Vote."
            ).setFontSize(8).setMarginTop(30));
        }
    }

    /**
     * Exporte les résultats d'une élection au format CSV.
     */
    public void exporterCSV(Election election, List<VoteService.ResultatCandidat> resultats,
                            String cheminFichier) throws IOException {

        try (CSVWriter writer = new CSVWriter(new FileWriter(cheminFichier))) {

            writer.writeNext(new String[]{"Election", election.getTitre()});
            writer.writeNext(new String[]{"Date debut", election.getDateDebut().format(FORMAT)});
            writer.writeNext(new String[]{"Date fin", election.getDateFin().format(FORMAT)});
            writer.writeNext(new String[]{}); // ligne vide pour la lisibilité

            writer.writeNext(new String[]{"Candidat", "Nombre de votes"});
            for (VoteService.ResultatCandidat resultat : resultats) {
                writer.writeNext(new String[]{
                        resultat.getCandidat().getNomComplet(),
                        String.valueOf(resultat.getNbVotes())
                });
            }
        }
    }
}