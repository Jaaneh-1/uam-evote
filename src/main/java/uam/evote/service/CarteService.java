package uam.evote.service;

import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import uam.evote.model.User;

import java.io.IOException;

public class CarteService {

    /**
     * Génère la carte d'électeur d'un utilisateur au format PDF,
     * avec un code-barres (Code 128) encodant son matricule.
     */
    public void genererCarte(User user, String cheminFichier) throws IOException {

        try (PdfWriter writer = new PdfWriter(cheminFichier);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc, PageSize.A6)) {

            document.setMargins(20, 20, 20, 20);

            document.add(new Paragraph("UAM e-Vote")
                    .setFontSize(14).setBold().setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("CARTE D'ÉLECTEUR")
                    .setFontSize(11).setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15));

            document.add(new Paragraph(user.getNomComplet())
                    .setFontSize(16).setBold().setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Matricule : " + user.getMatricule())
                    .setFontSize(11).setTextAlignment(TextAlignment.CENTER));

            String affectation = user.getRole() == User.Role.ETUDIANT
                    ? "Classe : " + (user.getClasse() != null ? user.getClasse() : "-")
                    : "Département : " + (user.getDepartement() != null ? user.getDepartement() : "-");

            document.add(new Paragraph(affectation)
                    .setFontSize(10).setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("UFR : " + (user.getUfr() != null ? user.getUfr() : "-"))
                    .setFontSize(10).setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15));

            // Code-barres encodant le matricule, utile pour un contrôle rapide
            // à l'entrée d'un bureau de vote (bonus, en plus du QR code d'authentification)
            Barcode128 barcode = new Barcode128(pdfDoc);
            barcode.setCode(user.getMatricule());
            Image barcodeImage = new Image(barcode.createFormXObject(ColorConstants.BLACK, ColorConstants.BLACK, pdfDoc));
            barcodeImage.setAutoScale(true);
            barcodeImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

            document.add(barcodeImage);

            document.add(new Paragraph("Cette carte atteste de l'inscription de son détenteur sur les listes électorales de l'UAM.")
                    .setFontSize(7).setTextAlignment(TextAlignment.CENTER).setMarginTop(15));
        }
    }
}