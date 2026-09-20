package uam.evote.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Référentiel académique de l'UAM : UFR -> Départements -> Classes.
 * Centralisé ici pour que le formulaire "Ajouter un utilisateur" (UFR/Département/Classe)
 * et le formulaire "Créer une élection" (Valeur cible) utilisent exactement les mêmes
 * données, sans duplication ni risque de faute de frappe entre les deux écrans.
 *
 * Pour ajouter l'École Polytechnique ou de nouvelles filières plus tard, il suffit
 * d'ajouter une entrée dans le bloc static ci-dessous ; aucune autre modification
 * de code n'est nécessaire, tous les écrans se mettent à jour automatiquement.
 */
public class ReferentielUAM {

    private static final Map<String, Map<String, List<String>>> DONNEES = new LinkedHashMap<>();

    static {
        Map<String, List<String>> departementsSTA = new LinkedHashMap<>();
        departementsSTA.put("Mathématiques, Informatique et Modélisation (MIM)", List.of(
                "L1 MPI",
                "L2 MPI",
                "L3 Informatique Appliquée (Informatique)",
                "L3 Mathématiques et Modélisation (MM)",
                "L1 MIASS",
                "L2 MIASS"
        ));
        departementsSTA.put("Sciences de la Matière et de l'Univers (SMU)", List.of(
                "L1 SML",
                "L2 SML",
                "L3 PA"
        ));
        DONNEES.put("UFR STA", departementsSTA);

        Map<String, List<String>> departementsTECNA = new LinkedHashMap<>();
        departementsTECNA.put("Sciences de l'Information et de la Communication (SIC)", List.of(
                "Communication & Marketing numériques",
                "Infographie / Design numérique"
        ));
        DONNEES.put("UFR TECNA", departementsTECNA);

        Map<String, List<String>> departementsSEG = new LinkedHashMap<>();
        departementsSEG.put("Économie", List.of());
        departementsSEG.put("Gestion", List.of());
        DONNEES.put("UFR SEG", departementsSEG);
    }

    public static List<String> listerUFR() {
        return List.copyOf(DONNEES.keySet());
    }

    public static List<String> listerDepartements(String ufr) {
        Map<String, List<String>> departements = DONNEES.get(ufr);
        return departements == null ? List.of() : List.copyOf(departements.keySet());
    }

    public static List<String> listerClasses(String ufr, String departement) {
        Map<String, List<String>> departements = DONNEES.get(ufr);
        if (departements == null || departement == null) {
            return List.of();
        }
        List<String> classes = departements.get(departement);
        return classes == null ? List.of() : classes;
    }

    public static List<String> listerToutesClasses() {
        return DONNEES.values().stream()
                .flatMap(deps -> deps.values().stream())
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<String> listerTousDepartements() {
        return DONNEES.values().stream()
                .flatMap(deps -> deps.keySet().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public static String trouverDepartementDeClasse(String ufr, String classe) {
        Map<String, List<String>> departements = DONNEES.get(ufr);
        if (departements == null || classe == null) {
            return null;
        }
        for (Map.Entry<String, List<String>> entree : departements.entrySet()) {
            if (entree.getValue().contains(classe)) {
                return entree.getKey();
            }
        }
        return null;
    }
}