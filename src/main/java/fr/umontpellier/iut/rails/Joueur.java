package fr.umontpellier.iut.rails;

import javax.management.monitor.CounterMonitor;
import java.util.*;
import java.util.stream.Collectors;

public class Joueur {

    /**
     * Les couleurs possibles pour les joueurs (pour l'interface graphique)
     */
    public static enum Couleur {
        JAUNE, ROUGE, BLEU, VERT, ROSE;
    }

    /**
     * Jeu auquel le joueur est rattaché
     */
    private Jeu jeu;
    /**
     * Nom du joueur
     */
    private String nom;
    /**
     * CouleurWagon du joueur (pour représentation sur le plateau)
     */
    private Couleur couleur;
    /**
     * Nombre de gares que le joueur peut encore poser sur le plateau
     */
    private int nbGares;
    /**
     * Nombre de wagons que le joueur peut encore poser sur le plateau
     */
    private int nbWagons;
    /**
     * Liste des missions à réaliser pendant la partie
     */
    private List<Destination> destinations;
    /**
     * Liste des cartes que le joueur a en main
     */
    private List<CouleurWagon> cartesWagon;
    /**
     * Liste temporaire de cartes wagon que le joueur est en train de jouer pour
     * payer la capture d'une route ou la construction d'une gare
     */
    private List<CouleurWagon> cartesWagonPosees;
    /**
     * Score courant du joueur (somme des valeurs des routes capturées)
     */
    private int score;

    public Joueur(String nom, Jeu jeu, Joueur.Couleur couleur) {
        this.nom = nom;
        this.jeu = jeu;
        this.couleur = couleur;
        nbGares = 3;
        nbWagons = 45;
        cartesWagon = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            this.cartesWagon.add(this.jeu.piocherCarteWagon());
        }
        cartesWagonPosees = new ArrayList<>();
        destinations = new ArrayList<>();
        score = 12; // chaque gare non utilisée vaut 4 points
    }

    public String getNom() {
        return nom;
    }

    public Couleur getCouleur() {
        return couleur;
    }

    public int getNbGares() {
        return nbGares;
    }

    public int getScore() {
        return score;
    }

    public int getNbWagons() {
        return nbWagons;
    }

    public Jeu getJeu() {
        return jeu;
    }

    public List<CouleurWagon> getCartesWagonPosees() {
        return cartesWagonPosees;
    }

    public List<CouleurWagon> getCartesWagon() {
        return cartesWagon;
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    /**
     * Attend une entrée de la part du joueur (au clavier ou sur la websocket) et
     * renvoie le choix du joueur.
     * <p>
     * Cette méthode lit les entrées du jeu ({@code Jeu.lireligne()}) jusqu'à ce
     * qu'un choix valide (un élément de {@code choix} ou de {@code boutons} ou
     * éventuellement la chaîne vide si l'utilisateur est autorisé à passer) soit
     * reçu.
     * Lorsqu'un choix valide est obtenu, il est renvoyé par la fonction.
     * <p>
     * Si l'ensemble des choix valides ({@code choix} + {@code boutons}) ne comporte
     * qu'un seul élément et que {@code canPass} est faux, l'unique choix valide est
     * automatiquement renvoyé sans lire l'entrée de l'utilisateur.
     * <p>
     * Si l'ensemble des choix est vide, la chaîne vide ("") est automatiquement
     * renvoyée par la méthode (indépendamment de la valeur de {@code canPass}).
     * <p>
     * Exemple d'utilisation pour demander à un joueur de répondre à une question
     * par "oui" ou "non" :
     * <p>
     * {@code
     * List<String> choix = Arrays.asList("Oui", "Non");
     * String input = choisir("Voulez vous faire ceci ?", choix, new ArrayList<>(), false);
     * }
     * <p>
     * <p>
     * Si par contre on voulait proposer les réponses à l'aide de boutons, on
     * pourrait utiliser :
     * <p>
     * {@code
     * List<String> boutons = Arrays.asList("1", "2", "3");
     * String input = choisir("Choisissez un nombre.", new ArrayList<>(), boutons, false);
     * }
     *
     * @param instruction message à afficher à l'écran pour indiquer au joueur la
     *                    nature du choix qui est attendu
     * @param choix       une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur
     * @param boutons     une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur qui doivent être
     *                    représentés par des boutons sur l'interface graphique.
     * @param peutPasser  booléen indiquant si le joueur a le droit de passer sans
     *                    faire de choix. S'il est autorisé à passer, c'est la
     *                    chaîne de caractères vide ("") qui signifie qu'il désire
     *                    passer.
     * @return le choix de l'utilisateur (un élément de {@code choix}, ou de
     * {@code boutons} ou la chaîne vide)
     */
    public String choisir(String instruction, Collection<String> choix, Collection<String> boutons,
                          boolean peutPasser) {
        // on retire les doublons de la liste des choix
        HashSet<String> choixDistincts = new HashSet<>();
        choixDistincts.addAll(choix);
        choixDistincts.addAll(boutons);

        // Aucun choix disponible
        if (choixDistincts.isEmpty()) {
            return "";
        } else {
            // Un seul choix possible (renvoyer cet unique élément)
            if (choixDistincts.size() == 1 && !peutPasser)
                return choixDistincts.iterator().next();
            else {
                String entree;
                // Lit l'entrée de l'utilisateur jusqu'à obtenir un choix valide
                while (true) {
                    jeu.prompt(instruction, boutons, peutPasser);
                    entree = jeu.lireLigne();
                    // si une réponse valide est obtenue, elle est renvoyée
                    if (choixDistincts.contains(entree) || (peutPasser && entree.equals("")))
                        return entree;
                }
            }
        }
    }

    /**
     * Affiche un message dans le log du jeu (visible sur l'interface graphique)
     *
     * @param message le message à afficher (peut contenir des balises html pour la
     *                mise en forme)
     */
    public void log(String message) {
        jeu.log(message);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(String.format("=== %s (%d pts) ===", nom, score));
        joiner.add(String.format("  Gares: %d, Wagons: %d", nbGares, nbWagons));
        joiner.add("  Destinations: "
                + destinations.stream().map(Destination::toString).collect(Collectors.joining(", ")));
        joiner.add("  Cartes wagon: " + CouleurWagon.listToString(cartesWagon));
        return joiner.toString();
    }

    /**
     * @return une chaîne de caractères contenant le nom du joueur, avec des balises
     * HTML pour être mis en forme dans le log
     */
    public String toLog() {
        return String.format("<span class=\"joueur\">%s</span>", nom);
    }

    /**
     * Renvoie une représentation du joueur sous la forme d'un objet Java simple
     * (POJO)
     */
    public Object asPOJO() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("nom", nom);
        data.put("couleur", couleur);
        data.put("score", score);
        data.put("nbGares", nbGares);
        data.put("nbWagons", nbWagons);
        data.put("estJoueurCourant", this == jeu.getJoueurCourant());
        data.put("destinations", destinations.stream().map(Destination::asPOJO).collect(Collectors.toList()));
        data.put("cartesWagon", cartesWagon.stream().sorted().map(CouleurWagon::name).collect(Collectors.toList()));
        data.put("cartesWagonPosees",
                cartesWagonPosees.stream().sorted().map(CouleurWagon::name).collect(Collectors.toList()));
        return data;
    }

    /**
     * Propose une liste de cartes destinations, parmi lesquelles le joueur doit en
     * garder un nombre minimum n.
     * <p>
     * Tant que le nombre de destinations proposées est strictement supérieur à n,
     * le joueur peut choisir une des destinations qu'il retire de la liste des
     * choix, ou passer (en renvoyant la chaîne de caractères vide).
     * <p>
     * Les destinations qui ne sont pas écartées sont ajoutées à la liste des
     * destinations du joueur. Les destinations écartées sont renvoyées par la
     * fonction.
     *
     * @param destinationsPossibles liste de destinations proposées parmi lesquelles
     *                              le joueur peut choisir d'en écarter certaines
     * @param n                     nombre minimum de destinations que le joueur
     *                              doit garder
     * @return liste des destinations qui n'ont pas été gardées par le joueur
     */
    public List<Destination> choisirDestinations(List<Destination> destinationsPossibles, int n) {
        List<Destination> destinationPasGardee = new ArrayList<>();
        ArrayList<String> transformToString = new ArrayList<>();
        boolean sort = false;
        String res = null;
        for (int i = 0; i < destinationsPossibles.size(); i++) {
            transformToString.add(destinationsPossibles.get(i).toString());
        }
        while (destinationsPossibles.size() > n && !Objects.equals(res, "")) {
            res = choisir("Choisissez au maximum 2 destinations a défausser : ", new ArrayList<>(), transformToString, true);
            if (!Objects.equals(res, "")) {
                for (int i = 0; i < destinationsPossibles.size(); i++) {
                    if (Objects.equals(res, destinationsPossibles.get(i).toString())) {
                        destinationPasGardee.add(destinationsPossibles.get(i)); //ajouter la destination à défausser dans une liste des cartes que l'on veut défausser
                        transformToString.remove(res); //enlever res des choix
                        destinationsPossibles.remove(destinationsPossibles.get(i)); //enlever la destination dans la liste des destinations proposées
                        break;
                    }
                }
            }
        }
        this.destinations.addAll(destinationsPossibles);
        return destinationPasGardee;
    }

    //Retourne le nombre de locomotive
    private int nbLocomotive() {
        int compteur = 0;
        for (CouleurWagon c : this.cartesWagon) {
            if (c.equals(CouleurWagon.LOCOMOTIVE)) compteur++;
        }
        return compteur;
    }

    //Retourne le nombre de couleur
    private int nbCouleur(CouleurWagon couleur) {
        int compteur = 0;
        for (CouleurWagon c : this.cartesWagon) {
            if (c.equals(couleur)) compteur++;
        }
        return compteur;
    }

    //Retourne le nombre de carte de même couleur
    private int nbCarteMemeCouleur() {
        CouleurWagon max = null;
        int nbMax = 0;
        for (CouleurWagon c : this.cartesWagon) {
            if (!c.equals(max) && !c.equals(CouleurWagon.LOCOMOTIVE)) {
                int maxTemp = 0;
                for (CouleurWagon c2 : this.cartesWagon) {
                    if (c2.equals(c)) {
                        maxTemp++;
                    }
                }
                if (maxTemp > nbMax) {
                    nbMax = maxTemp;
                    max = c;
                }

            }
        }
        return nbMax;
    }

    private int calcNbPoints(int tailleRoute) {
        int points = 0;
        switch (tailleRoute) {
            case 1:
                points = 1;
                break;
            case 2:
                points = 2;
                break;
            case 3:
                points = 4;
                break;
            case 4:
                points = 7;
                break;
            case 6:
                points = 15;
                break;
            case 8:
                points = 21;
                break;
            default:
                break;
        }
        return points;
    }

    private boolean possedeDejaRoute(Route route) {
        boolean res = false;
        for (Route r : this.jeu.getRoutes()) {
            if (r.getVille1().equals(route.getVille1()) && r.getVille2().equals(route.getVille2()) && this.equals(r.getProprietaire())) {
                res = true;
                break;
            }
        }
        return res;
    }

    private boolean routeDejaPossede(Route route) {
        boolean res = false;
        if (this.jeu.getJoueurs().size() <= 3) {
            for (Route r : this.jeu.getRoutes()) {
                if (r.getVille1().equals(route.getVille1()) && r.getVille2().equals(route.getVille2()) && !route.equals(r)) {
                    r.setPrenable(false);
                    route.setPrenable(false);
                    break;
                }
                return false;
            }
        }
        for (Route r : this.jeu.getRoutes()) {
            if (r.getVille1().equals(route.getVille1()) && r.getVille2().equals(route.getVille2()) && this.equals(r.getProprietaire())) {
                res = true;
                break;
            }
        }
        return res;
    }

    private int nbCartesAPoserEnPlus(Route route) {
        int result = 0;
        ArrayList<CouleurWagon> test = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            CouleurWagon pioche = this.jeu.piocherCarteWagon();
            test.add(pioche);
            this.jeu.defausserCarteWagon(pioche);
            if (pioche.equals(route.getCouleur()) || pioche.equals(CouleurWagon.LOCOMOTIVE)) {
                result++;
            }
        }
        return result;
    }

    /**
     * Exécute un tour de jeu du joueur.
     * <p>
     * Cette méthode attend que le joueur choisisse une des options suivantes :
     * - le nom d'une carte wagon face visible à prendre ;
     * - le nom "GRIS" pour piocher une carte wagon face cachée s'il reste des
     * cartes à piocher dans la pile de pioche ou dans la pile de défausse ;
     * - la chaîne "destinations" pour piocher des cartes destination ;
     * - le nom d'une ville sur laquelle il peut construire une gare (ville non
     * prise par un autre joueur, le joueur a encore des gares en réserve et assez
     * de cartes wagon pour construire la gare) ;
     * - le nom d'une route que le joueur peut capturer (pas déjà capturée, assez de
     * wagons et assez de cartes wagon) ;
     * - la chaîne de caractères vide pour passer son tour
     * <p>
     * Lorsqu'un choix valide est reçu, l'action est exécutée (il est possible que
     * l'action nécessite d'autres choix de la part de l'utilisateur, comme "choisir les cartes wagon à défausser pour capturer une route" ou
     * "construire une gare", "choisir les destinations à défausser", etc.)
     */

    public void jouerTour() {
        ArrayList<String> pasUtile = new ArrayList<>();
        ArrayList<String> choix = new ArrayList<>();

        // Ajout de "destinations" au choix
        choix.add("destinations");

        // Ajout de "GRIS" aux choix si la pile de carte wagon ou la defausse est vide
        if (!this.jeu.getPileCartesWagon().isEmpty() || !this.jeu.getDefausseCartesWagon().isEmpty()) {
            choix.add("GRIS");
        }

        // Ajout de toutes les cartes wagons visibles dans une liste
        ArrayList<String> choixCartesWagonsVisibles = new ArrayList<>();
        for (int i = 0; i < jeu.getCartesWagonVisibles().size(); i++) {
            choixCartesWagonsVisibles.add(jeu.getCartesWagonVisibles().get(i).name());
        }

        // Ajout des cartes wagons visibles au choix
        choix.addAll(choixCartesWagonsVisibles);

        // Ajout du nom des différentes villes dans une liste
        ArrayList<String> choixVilles = new ArrayList<>();
        // Si le joueur peut encore construire des gares
        if (this.nbGares > 0 && (this.nbGares == 3 ? 1 : this.nbGares == 2 ? 2 : 3) <= this.nbLocomotive() + this.nbCarteMemeCouleur()) {
            for (Ville v : this.jeu.getVilles()) {
                //si la ville n'a pas de propriétaire
                if (v.getProprietaire() == null) {
                    choixVilles.add(v.toString());
                }
            }
        }

        // Ajout des différentes villes au choix
        choix.addAll(choixVilles);

        //Ajout des différentes routes et ferry
        ArrayList<String> choixRoutes = new ArrayList<>();
        for (Route r : this.jeu.getRoutes()) {
            // Conditions :
            // - si la route n'a pas de propriétaire et que la longueur de la route est <= au nombre de cartes possédés de la couleur de la route + le nombre de locomotives
            // - ou que la route est grise et que la longueur de la route est <= au nombre de cartes de la même couleurs + le nombre de locomotives
            // -
            if(r.getProprietaire() == null && nbWagons >= r.getLongueur() && (r.getLongueur() <= this.nbCouleur(r.getCouleur()) + nbLocomotive() && r.getClass()!=Ferry.class)
                    ||  r.getCouleur().equals(CouleurWagon.GRIS) && r.getLongueur() <= this.nbCarteMemeCouleur() + nbLocomotive() && r.getClass()!=Ferry.class
                    || (r.getClass() == Ferry.class
                    && ((Ferry) r).getNbLocomotives() <= nbLocomotive()
                    && r.getLongueur() <= this.nbCarteMemeCouleur() + nbLocomotive()
                    && r.getProprietaire() == null
                    && nbWagons >= r.getLongueur())){
                if (r.isPrenable())
                if (!this.routeDejaPossede(r)) {
                    if (r.getClass() != Tunnel.class) {
                        choixRoutes.add(r.getNom());
                    }
                }
            }
        }
        log(choixRoutes + "");
        choix.addAll(choixRoutes);


        //Ajout des différents tunnels
        ArrayList<String> choixTunnel = new ArrayList<>();
        for (Route r : this.jeu.getRoutes()) {
            if (r instanceof Tunnel && r.getProprietaire() == null && nbWagons >= r.getLongueur()
                    && (r.getLongueur() <= this.nbCouleur(r.getCouleur()) + nbLocomotive()) || r instanceof Tunnel && r.getCouleur().equals(CouleurWagon.GRIS)
                    && r.getLongueur() <= this.nbCarteMemeCouleur() + nbLocomotive()) {
                choixTunnel.add(r.getNom());
            }
        }
        choix.addAll(choixTunnel);


        // Premier choix du joueur
        String choixUtilisateur = choisir("Faites une action", choix, pasUtile, true);

        // Création de la main du joueur (String) en y ajoutant toutes les cartes wagons du joueur (CouleurWagon)
        ArrayList<String> mainDuJoueur = new ArrayList<>();
        for (CouleurWagon w : this.cartesWagon) mainDuJoueur.add(w.name());

        // Si l'utilisateur choisit une route ou ferry
        for (Route routeChoisie : this.jeu.getRoutes()) {
            if (choixRoutes.contains(routeChoisie.getNom())
                    && routeChoisie.getNom().equals(choixUtilisateur)
                    && routeChoisie.getProprietaire() == null
                    && !(routeChoisie instanceof Tunnel)) {
                int tailleRoute = routeChoisie.getLongueur();
                int nbLoco = nbLocomotive();
                if (routeChoisie instanceof Ferry) {
                    for (int i = 0; i < ((Ferry) routeChoisie).getNbLocomotives(); i++) {
                        cartesWagonPosees.add(CouleurWagon.LOCOMOTIVE);
                        cartesWagon.remove(CouleurWagon.LOCOMOTIVE);
                    }
                }
                String variableRoute = routeChoisie.getCouleur().equals(CouleurWagon.GRIS) ? null : routeChoisie.getCouleur().name();
                while (this.cartesWagonPosees.size() != tailleRoute) {
                    String choixCartesPourCreerRoutes = choisir("Choisissez des cartes pour construire la route " + choixUtilisateur, mainDuJoueur, new ArrayList<>(), false);
                    for (CouleurWagon c : this.cartesWagon) {
                        // - si le joueur possède la carte qu'il a choisit
                        if (c.name().equals(choixCartesPourCreerRoutes)) {
                            if (nbLoco + nbCouleur(c) >= tailleRoute && variableRoute == null
                                    || c.equals(CouleurWagon.LOCOMOTIVE)
                                    || c.name().equals(variableRoute)) {
                                if (!c.equals(CouleurWagon.LOCOMOTIVE)) variableRoute = c.name();
                                this.cartesWagonPosees.add(c);
                                this.cartesWagon.remove(c);
                                mainDuJoueur.remove(choixCartesPourCreerRoutes);
                                break;
                            }
                        }
                    }
                }
                routeChoisie.setProprietaire(this);
                this.jeu.getDefausseCartesWagon().addAll(this.cartesWagonPosees);
                this.cartesWagonPosees.clear();
                choixRoutes.remove(choixUtilisateur);
                choix.remove(choixUtilisateur);
                nbWagons -= tailleRoute;
                this.score += calcNbPoints(tailleRoute);
            }
        }

        // Si l'utilisateur choisit un tunnel
        for (Route routeChoisie : this.jeu.getRoutes()) {
            if (choixTunnel.contains(routeChoisie.getNom())
                    && routeChoisie.getNom().equals(choixUtilisateur)
                    && routeChoisie.getProprietaire() == null
                    && routeChoisie instanceof Tunnel) {
                int tailleRoute = routeChoisie.getLongueur();
                int nbLoco = nbLocomotive();
                String variableTunnel = routeChoisie.getCouleur().equals(CouleurWagon.GRIS) ? null : routeChoisie.getCouleur().name();
                // Pioche, défausse, stocke, le nombre de cartes à posées en plus.
                String choixCartesPourCreerTunnel = null;
                while (this.cartesWagonPosees.size() != tailleRoute) {
                    choixCartesPourCreerTunnel = choisir("Choisissez des cartes " + routeChoisie.getCouleur() + " pour construire la route pour construire le tunnel " + choixUtilisateur, mainDuJoueur, new ArrayList<>(), true);
                    for (CouleurWagon c : this.cartesWagon) {
                        // - si le joueur possède la carte qu'il a choisit
                        if (c.name().equals(choixCartesPourCreerTunnel)) {
                            if (nbLoco + nbCouleur(c) >= tailleRoute && variableTunnel == null
                                    || c.equals(CouleurWagon.LOCOMOTIVE)
                                    || c.name().equals(variableTunnel)) {
                                if (!c.equals(CouleurWagon.LOCOMOTIVE)) variableTunnel = c.name();
                                log("variabletunnel " + variableTunnel);
                                log("choixCarte " + choixCartesPourCreerTunnel);
                                this.cartesWagonPosees.add(c);
                                this.cartesWagon.remove(c);
                                mainDuJoueur.remove(choixCartesPourCreerTunnel);
                                break;
                            }
                        }
                    }
                }
                int nbCartesPoserEnPlus = nbCartesAPoserEnPlus(routeChoisie);
                choixCartesPourCreerTunnel = "     ";
                if (nbCartesPoserEnPlus > 0) {
                    while (this.cartesWagonPosees.size() != tailleRoute + nbCartesPoserEnPlus && !choixCartesPourCreerTunnel.equals("")) {
                        boolean correct = false;
                        for (int i = 0; i<this.cartesWagon.size(); i++){
                            if (this.cartesWagon.get(i).name().equals(variableTunnel) || this.cartesWagon.get(i).equals(CouleurWagon.LOCOMOTIVE)){
                                correct = true;
                            }
                        }
                        if (!correct){
                            mainDuJoueur = new ArrayList<>();
                        }
                        choixCartesPourCreerTunnel = choisir("(Supplément : " + nbCartesPoserEnPlus + " cartes " + routeChoisie.getCouleur() + " ou Locomotive)", mainDuJoueur, new ArrayList<>(), true);
                        log("-"+choixCartesPourCreerTunnel+"-");
                        for (CouleurWagon c : this.cartesWagon) {
                            // - si le joueur possède la carte qu'il a choisit
                            if (c.name().equals(choixCartesPourCreerTunnel)) {
                                if (c.equals(CouleurWagon.LOCOMOTIVE) || c.name().equals(variableTunnel)) {
                                    if (!c.equals(CouleurWagon.LOCOMOTIVE)) variableTunnel = c.name();
                                    this.cartesWagonPosees.add(c);
                                    this.cartesWagon.remove(c);
                                    mainDuJoueur.remove(choixCartesPourCreerTunnel);
                                    break;
                                }
                            }
                        }
                    }
                    if (choixCartesPourCreerTunnel.equals("")) {
                        this.cartesWagon.addAll(cartesWagonPosees);
                        cartesWagonPosees.clear();
                    }
                    else {
                        routeChoisie.setProprietaire(this);
                        this.jeu.getDefausseCartesWagon().addAll(this.cartesWagonPosees);
                        this.cartesWagonPosees.clear();
                        choixRoutes.remove(choixUtilisateur);
                        choix.remove(choixUtilisateur);
                        nbWagons -= tailleRoute;
                        this.score += calcNbPoints(tailleRoute);
                        break;
                    }
                }
                else if (nbCartesPoserEnPlus == 0) {
                    routeChoisie.setProprietaire(this);
                    this.jeu.getDefausseCartesWagon().addAll(this.cartesWagonPosees);
                    this.cartesWagonPosees.clear();
                    choixRoutes.remove(choixUtilisateur);
                    choix.remove(choixUtilisateur);
                    nbWagons -= tailleRoute;
                    this.score += calcNbPoints(tailleRoute);
                    break;
                }
            }
        }


        // Si l'utilisateur choisit une ville
        for (Ville villeChoisie : this.jeu.getVilles()) {
            // Condition :
            // - si le choix de l'utilisateur est égal à une ville
            // - qui est contenue dans la liste des villes (choixVilles)
            // - et qui n'a pas de propriétaire
            if (choixVilles.contains(villeChoisie.getNom())
                    && villeChoisie.getNom().equals(choixUtilisateur)
                    && villeChoisie.getProprietaire() == null) {
                int coutGareEnCarte = 0;
                // Calcul des cartes qu'il faut en fonction du nombre de gare de la personne
                switch (nbGares) {
                    case 1:
                        coutGareEnCarte = 3;
                        break;
                    case 2:
                        coutGareEnCarte = 2;
                        break;
                    case 3:
                        coutGareEnCarte = 1;
                    default:
                }
                // Associe la fonction nbLocomotive à nbL pour ne pas modifier sa valeur par la suite
                int nbLocomotive = nbLocomotive();
                // Variable mise à null utile pour les suites de cartes
                String variable = null;
                // Tant que la pile des cartes posées n'est pas égale au nombre de carte que le joueur doit posé
                while (this.cartesWagonPosees.size() != coutGareEnCarte) {
                    String choixCartePourCreerGare = choisir("Choisissez une carte dans votre jeu pour créer la gare à " + choixUtilisateur, mainDuJoueur, new ArrayList<>(), false);
                    for (CouleurWagon couleur : this.cartesWagon) {
                        // Si l'utilisateur choisit une carte qu'il possède
                        if (couleur.name().equals(choixCartePourCreerGare)) {
                            // Condition :
                            // - si le nombre de locomotive dans la main du joueur plus le nombre de couleur total dans la main du joueur est >= au cout en carte de la garre et que la variable == null OU
                            // - si le choix du joueur est une carte locomotive OU
                            // - si le choix du joueur est égal à la variable
                            if (nbLocomotive + nbCouleur(couleur) >= coutGareEnCarte && variable == null
                                    || couleur.equals(CouleurWagon.LOCOMOTIVE) || couleur.name().equals(variable)) {
                                // Condition :
                                // - si le choix du joueur n'est pas une locomotive
                                if (!couleur.equals(CouleurWagon.LOCOMOTIVE)) variable = couleur.name();
                                this.cartesWagonPosees.add(couleur);
                                this.cartesWagon.remove(couleur);
                                mainDuJoueur.remove(choixCartePourCreerGare);
                                break;
                            }
                        }
                    }
                }
                this.score -= 4;
                villeChoisie.setProprietaire(this);
                this.jeu.getDefausseCartesWagon().addAll(this.cartesWagonPosees);
                this.cartesWagonPosees.clear();
                choix.remove(choixUtilisateur);
                choixVilles.remove(choixUtilisateur);
                nbGares--;
            }
        }

        // Si l'utilisateur choisit de prendre une carte visible ou de piocher dans la liste des carte wagons
        if (choixCartesWagonsVisibles.contains(choixUtilisateur) || choixUtilisateur.equals("GRIS")) {
            // Création d'une liste de CouleurWagon temporaire
            ArrayList<CouleurWagon> cartesWagonVisibleTemps1 = new ArrayList<>(this.jeu.getCartesWagonVisibles());
            for (CouleurWagon couleurWagon1 : cartesWagonVisibleTemps1) {
                // [PREMIER CHOIX] Si l'utilisateur choisit de piocher une carte locomotive visible
                if (choixUtilisateur.equals(CouleurWagon.LOCOMOTIVE.name())) {
                    jeu.retirerCarteWagonVisible(CouleurWagon.LOCOMOTIVE);
                    this.cartesWagon.add(CouleurWagon.LOCOMOTIVE);
                    break;
                }
                // [PREMIER CHOIX] Si l'utilisateur choisit de prendre une carte visible
                else if (couleurWagon1.name().equals(choixUtilisateur)) {
                    cartesWagonVisibleTemps1.remove(couleurWagon1);
                    this.cartesWagon.add(couleurWagon1);
                    choix.remove(choixUtilisateur);
                    cartesWagonVisibleTemps1.add(this.jeu.piocherCarteWagon());
                    this.jeu.getCartesWagonVisibles().clear();
                    this.jeu.getCartesWagonVisibles().addAll(cartesWagonVisibleTemps1);
                    // Initialisation d'une nouvelle liste de choix sans les locomotives
                    ArrayList<String> choixBis1 = new ArrayList<>();
                    choixBis1.add("GRIS");
                    for (CouleurWagon couleurWagon : this.jeu.getCartesWagonVisibles()) {
                        if (couleurWagon != CouleurWagon.LOCOMOTIVE) {
                            choixBis1.add(couleurWagon.name());
                        }
                    }
                    // Deuxième choix de carte
                    String choixUtilisateur2 = choisir("Faites un deuxieme choix", choixBis1, pasUtile, false);
                    // Création d'une liste de CouleurWagon temporaire
                    ArrayList<CouleurWagon> cartesWagonVisibleTemps2 = new ArrayList<>(this.jeu.getCartesWagonVisibles());
                    for (CouleurWagon couleurWagon2 : cartesWagonVisibleTemps2) {
                        // [DEUXIEME CHOIX] Si l'utilisateur choisit de prendre une autre carte visible
                        if (couleurWagon2.name().equals(choixUtilisateur2)) {
                            cartesWagonVisibleTemps2.remove(couleurWagon2);
                            this.cartesWagon.add(couleurWagon2);
                            choixBis1.remove(choixUtilisateur2);
                            cartesWagonVisibleTemps2.add(this.jeu.piocherCarteWagon());
                            this.jeu.getCartesWagonVisibles().clear();
                            this.jeu.getCartesWagonVisibles().addAll(cartesWagonVisibleTemps2);
                            break;
                        }
                        // [DEUXIEME CHOIX] Si l'utilisateur choisit de piocher dans la pile des cartes wagons
                        else if (choixUtilisateur2.equals("GRIS")) {
                            this.cartesWagon.add(this.jeu.piocherCarteWagon());
                            break;
                        }
                    }
                    break;
                }
                // [PREMIER CHOIX] Si l'utilisateur choisit de piocher dans la pile des cartes wagons
                else if (choixUtilisateur.equals("GRIS")) {
                    this.cartesWagon.add(this.jeu.piocherCarteWagon());
                    // Initialisation d'une nouvelle liste de choix sans les locomotives
                    ArrayList<String> choixBis2 = new ArrayList<>();
                    choixBis2.add("GRIS");
                    for (CouleurWagon couleurWagon : this.jeu.getCartesWagonVisibles()) {
                        if (couleurWagon != CouleurWagon.LOCOMOTIVE) {
                            choixBis2.add(couleurWagon.name());
                        }
                    }
                    // Deuxième choix de carte
                    String choixUtilisateur2 = choisir("Faites un deuxieme choix", choixBis2, pasUtile, false);
                    // Création d'une liste de CouleurWagon temporaire
                    ArrayList<CouleurWagon> cartesWagonVisibleTemps2 = new ArrayList<>(this.jeu.getCartesWagonVisibles());
                    for (CouleurWagon couleurWagon2 : cartesWagonVisibleTemps2) {
                        // [DEUXIEME CHOIX] Si l'utilisateur choisit de piocher dans la pile des cartes wagons
                        if (choixUtilisateur2.equals("GRIS")) {
                            this.cartesWagon.add(this.jeu.piocherCarteWagon());
                            break;

                        }
                        // [DEUXIEME CHOIX] Si l'utilisateur choisit de prendre une carte visible
                        else if (couleurWagon2.name().equals(choixUtilisateur2)) {
                            cartesWagonVisibleTemps2.remove(couleurWagon2);
                            this.cartesWagon.add(couleurWagon2);
                            choixBis2.remove(choixUtilisateur2);
                            cartesWagonVisibleTemps2.add(this.jeu.piocherCarteWagon());
                            this.jeu.getCartesWagonVisibles().clear();
                            this.jeu.getCartesWagonVisibles().addAll(cartesWagonVisibleTemps2);
                            break;
                        }
                    }
                    break;
                }
            }
        }

        // Si l'utilisateur choisit une destinations
        else if (choixUtilisateur.equals("destinations")) {
            // Création d'une liste de 3 cartes destinations piochées à présentées au joueur
            ArrayList<Destination> destinationsPiochees = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                destinationsPiochees.add(this.jeu.piocherDestination());
            }
            List<Destination> replacerCarteNonVouluesDansPileDestinations = this.choisirDestinations(destinationsPiochees, 1);
            // Ajoute la liste des cartes non voulues sous le paquet des cartes destinations
            jeu.getPileDestinations().addAll(replacerCarteNonVouluesDansPileDestinations);
        }

        // Si l'utilisateur choisit une route
    }
}

