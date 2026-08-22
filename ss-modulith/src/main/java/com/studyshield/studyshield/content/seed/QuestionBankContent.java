package com.studyshield.studyshield.content.seed;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Curated seed question bank, age/class appropriate per band (issue #1).
 * <p>
 * Bands seeded today: Sr KG (age 4-5), Class 1 through Class 10, plus the Exp promo band.
 * Classes 2-10 carry four subjects each, 10 questions per subject, with a difficulty ramp:
 * early classes stay on readable SINGLE_CHOICE/TRUE_FALSE, upper classes cover board-level
 * topics (algebra, trigonometry, electricity, civics). Content follows the common CBSE/ICSE
 * core and is seeded against the board-agnostic ALL board.
 * <p>
 * To add questions later: append entries here or create questions via the admin
 * content APIs ({@code /api/v1/questions}). This class is a script path, not a runtime dependency.
 */
public final class QuestionBankContent {

    private QuestionBankContent() {}

    /**
     * @param text      question text
     * @param trueFalse true → options are [True, False]
     * @param options   answer choices (already includes True/False for trueFalse)
     * @param correct   option text of the single correct answer
     */
    public record SeedQuestion(String text, boolean trueFalse, List<String> options, String correct) {}

    public static final String BAND_SR_KG = "Sr KG";
    public static final String BAND_CLASS_1 = "Class 1";
    public static final String BAND_EXP = "Exp";
    public static final int MIN_CURATED_CLASS = 2;
    public static final int MAX_CURATED_CLASS = 10;

    /** Band keys for Class 2..Class 10, in order. */
    public static final List<String> CLASS_BANDS = java.util.stream.IntStream
            .rangeClosed(MIN_CURATED_CLASS, MAX_CURATED_CLASS)
            .mapToObj(n -> BAND_CLASS_1.replace("1", String.valueOf(n)))
            .toList();

    /** Maps a normalized class name to its band key, or null when no curated bank exists. */
    public static String bandForClassName(String normalizedClassName) {
        if (normalizedClassName == null) return null;
        String t = normalizedClassName.trim().toLowerCase(Locale.ROOT);
        if (t.contains("sr") || t.contains("senior") || t.contains("ukg")) return BAND_SR_KG;
        if (t.equals("exp") || t.equals("experimental") || t.equals("promo")) return BAND_EXP;
        int n = classNumber(t);
        if (n == 1) return BAND_CLASS_1;
        if (n >= MIN_CURATED_CLASS && n <= MAX_CURATED_CLASS) return "Class " + n;
        return null;
    }

    private static int classNumber(String lowerCased) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?:class|grade|std)?\\s*(\\d{1,2})").matcher(lowerCased);
        return m.matches() ? Integer.parseInt(m.group(1)) : -1;
    }

    /** Maps a child's age to a reasonable class band so sessions can filter by age alone. */
    public static String classNameForAge(int age) {
        if (age <= 3) return "Nursery";
        if (age <= 5) return BAND_SR_KG;
        if (age <= 7) return BAND_CLASS_1;
        return "Class " + Math.min(age - 5, 12);
    }

    /** tf() helper: TRUE_FALSE question. */
    private static SeedQuestion tf(String text, boolean answer) {
        return new SeedQuestion(text, true, List.of("True", "False"), answer ? "True" : "False");
    }

    /** mcq() helper: SINGLE_CHOICE question with 4 options. */
    private static SeedQuestion mcq(String text, String correct, String... others) {
        return new SeedQuestion(text, false, List.of(correct, others[0], others[1], others[2]), correct);
    }

    public static final Map<String, Map<String, List<SeedQuestion>>> BANK = Map.ofEntries(
            Map.entry(BAND_SR_KG, Map.of(
                    "Math", List.of(
                            mcq("How many fingers are on one hand?", "5", "4", "6", "10"),
                            mcq("What is 2 + 1?", "3", "2", "4", "5"),
                            mcq("Which animal is the biggest?", "Elephant", "Ant", "Cat", "Mouse"),
                            mcq("Which shape has three sides?", "Triangle", "Circle", "Square", "Star"),
                            mcq("Count the stars: ★ ★ ★ ★. How many?", "4", "3", "5", "6"),
                            mcq("What is 5 − 1?", "4", "3", "5", "2"),
                            mcq("Which number comes after 7?", "8", "6", "9", "7"),
                            mcq("A ball looks like which shape?", "Circle", "Square", "Triangle", "Rectangle"),
                            mcq("Which is the smallest number?", "2", "5", "9", "7"),
                            mcq("What is 3 + 2?", "5", "4", "6", "7"),
                            mcq("How many wheels does a bicycle have?", "2", "3", "4", "1"),
                            mcq("The sun looks like which shape?", "Circle", "Square", "Triangle", "Star"),
                            mcq("Which number do we start counting with?", "1", "0", "2", "10"),
                            mcq("What is 4 − 2?", "2", "1", "3", "4"),
                            tf("3 is more than 5.", false)
                    ),
                    "EVS", List.of(
                            mcq("We smell with our…", "Nose", "Eyes", "Ears", "Hands"),
                            mcq("Which animal gives us milk?", "Cow", "Dog", "Cat", "Hen"),
                            mcq("A baby dog is called a…", "Puppy", "Kitten", "Calf", "Chick"),
                            mcq("We see with our…", "Eyes", "Nose", "Ears", "Feet"),
                            mcq("Which one is a fruit?", "Mango", "Carrot", "Potato", "Onion"),
                            mcq("How many legs does a bird have?", "2", "4", "6", "8"),
                            mcq("We hear with our…", "Ears", "Eyes", "Nose", "Mouth"),
                            mcq("Which animal says 'meow'?", "Cat", "Dog", "Cow", "Lion"),
                            mcq("Which body part helps us walk?", "Legs", "Ears", "Eyes", "Nose"),
                            tf("We should drink water every day.", true),
                            mcq("A baby cow is called a…", "Calf", "Puppy", "Kitten", "Duckling"),
                            mcq("Which one can fly?", "Butterfly", "Dog", "Fish", "Cow"),
                            mcq("We taste with our…", "Tongue", "Nose", "Ears", "Hands"),
                            mcq("A fish lives in…", "Water", "A tree", "The sky", "A nest"),
                            tf("The sun rises at night.", false)
                    ),
                    "English", List.of(
                            mcq("Which letter comes after A?", "B", "C", "Z", "A"),
                            mcq("'Apple' starts with which letter?", "A", "B", "M", "S"),
                            mcq("What is the opposite of big?", "Small", "Tall", "Fat", "Long"),
                            mcq("Which one is a vowel?", "a", "b", "c", "d"),
                            mcq("'Bat' rhymes with…", "Cat", "Cup", "Sun", "Dog"),
                            mcq("'Elephant' starts with which letter?", "E", "F", "A", "L"),
                            mcq("What is the opposite of hot?", "Cold", "Warm", "Wet", "Fast"),
                            tf("'Ball' starts with the letter B.", true),
                            mcq("One cat, two…", "Cats", "Cat", "Cates", "Cati"),
                            mcq("Which word names an animal?", "Lion", "Red", "Jump", "Hot"),
                            mcq("Fill the missing letter: C_T (cat)", "a", "o", "u", "e"),
                            mcq("What is the opposite of up?", "Down", "Top", "Over", "High"),
                            mcq("'Sun' starts with which letter?", "S", "F", "M", "B"),
                            mcq("Which word is a colour?", "Blue", "Run", "Table", "Jump"),
                            tf("We write from left to right.", true)
                    ),
                    "General Knowledge", List.of(
                            mcq("Red light on traffic signals means…", "Stop", "Go", "Run", "Dance"),
                            mcq("Who treats us when we are sick?", "Doctor", "Teacher", "Farmer", "Driver"),
                            mcq("Which vehicle flies in the sky?", "Aeroplane", "Bus", "Ship", "Car"),
                            mcq("What colour is grass?", "Green", "Red", "Blue", "Black"),
                            mcq("Who teaches us in school?", "Teacher", "Doctor", "Postman", "Cook"),
                            mcq("How many colours are in a rainbow?", "7", "3", "5", "10"),
                            mcq("Which is the biggest land animal?", "Elephant", "Horse", "Dog", "Goat"),
                            mcq("A firefighter puts out…", "Fire", "Light", "Water", "Food"),
                            mcq("What colour is a banana?", "Yellow", "Blue", "Purple", "Black"),
                            mcq("Which animal lives in water?", "Fish", "Cow", "Hen", "Monkey"),
                            mcq("We wear shoes on our…", "Feet", "Hands", "Head", "Ears"),
                            mcq("Which is the national bird of India?", "Peacock", "Crow", "Parrot", "Hen"),
                            tf("We should brush our teeth every morning.", true),
                            mcq("Who brings us letters?", "Postman", "Pilot", "Chef", "Tailor"),
                            mcq("How many wheels does a car have?", "4", "2", "3", "6")
                    )
            )),
            Map.entry(BAND_CLASS_1, Map.of(
                    "Math", List.of(
                            mcq("What is 10 + 5?", "15", "12", "20", "25"),
                            mcq("What is 9 − 4?", "5", "4", "6", "13"),
                            mcq("Which number is even?", "2", "3", "5", "7"),
                            mcq("Which number comes between 4 and 6?", "5", "3", "7", "8"),
                            mcq("What is 7 + 3?", "10", "9", "11", "4"),
                            tf("12 comes just after 11.", true),
                            mcq("What is 20 − 10?", "10", "0", "15", "30"),
                            mcq("Which is the smallest number?", "3", "8", "15", "9"),
                            mcq("What is 6 + 6?", "12", "10", "13", "16"),
                            mcq("Half of 10 is…", "5", "2", "4", "10"),
                            mcq("Which coin has the biggest value?", "₹10", "₹1", "₹2", "₹5"),
                            mcq("How many days are there in a week?", "7", "5", "10", "12"),
                            mcq("Complete the pattern: 15, 16, 17, ___", "18", "19", "14", "20"),
                            mcq("What is 8 − 8?", "0", "1", "8", "16"),
                            tf("5 + 5 equals 11.", false)
                    ),
                    "EVS", List.of(
                            mcq("Which one is a living thing?", "Plant", "Stone", "Chair", "Ball"),
                            mcq("How many sense organs do we have?", "5", "2", "3", "10"),
                            mcq("We get wool from…", "Sheep", "Cow", "Hen", "Goat"),
                            mcq("Which season is the coldest?", "Winter", "Summer", "Spring", "Monsoon"),
                            mcq("Plants need ___ to grow.", "Water", "Plastic", "Toys", "Stones"),
                            tf("The sun gives us light.", true),
                            mcq("Which one is NOT a fruit?", "Potato", "Apple", "Banana", "Mango"),
                            mcq("We breathe in…", "Air", "Water", "Milk", "Smoke"),
                            mcq("A cow usually lives in a…", "Shed", "Nest", "Burrow", "Web"),
                            mcq("Which sense organ tells us something is hot?", "Skin", "Ear", "Eye", "Tongue"),
                            mcq("A baby frog is called a…", "Tadpole", "Calf", "Puppy", "Chick"),
                            tf("Green plants can make their own food.", true),
                            mcq("Water kept in a freezer becomes…", "Ice", "Steam", "Milk", "Smoke"),
                            mcq("Which animal lays eggs?", "Hen", "Cow", "Dog", "Cat"),
                            mcq("Rain comes from…", "Clouds", "The moon", "Stars", "Rivers")
                    ),
                    "English", List.of(
                            mcq("What is the opposite of day?", "Night", "Noon", "Week", "Sun"),
                            mcq("One box, two…", "Boxes", "Boxs", "Boxies", "Box"),
                            mcq("___ apple a day keeps the doctor away.", "An", "A", "Of", "On"),
                            mcq("Which word rhymes with 'sun'?", "Fun", "Fan", "Sonk", "Sand"),
                            mcq("What is the opposite of open?", "Close", "Big", "Wide", "Free"),
                            tf("The letter E is a vowel.", true),
                            mcq("Which word names a thing (naming word)?", "Table", "Run", "Blue", "Quickly"),
                            mcq("What is the opposite of fast?", "Slow", "Quick", "Late", "Early"),
                            mcq("A sentence begins with a ___ letter.", "Capital", "Small", "Red", "Round"),
                            mcq("Which word rhymes with 'cake'?", "Lake", "Cook", "Corn", "Cup"),
                            mcq("What is the opposite of first?", "Last", "Next", "Start", "Best"),
                            mcq("Choose the right word: I ___ to school.", "go", "goes", "gone", "going"),
                            tf("We put a full stop at the end of a sentence.", true),
                            mcq("What is the opposite of happy?", "Sad", "Glad", "Smiling", "Kind"),
                            mcq("'Kite' starts with which letter?", "K", "C", "J", "L")
                    ),
                    "General Knowledge", List.of(
                            mcq("What is the capital of India?", "New Delhi", "Mumbai", "Kolkata", "Chennai"),
                            mcq("Which is the national animal of India?", "Tiger", "Lion", "Elephant", "Leopard"),
                            mcq("Which is the national flower of India?", "Lotus", "Rose", "Sunflower", "Marigold"),
                            mcq("Which festival is called the festival of lights?", "Diwali", "Holi", "Eid", "Christmas"),
                            mcq("How many colours are on the Indian flag?", "3", "2", "4", "5"),
                            tf("The peacock is the national bird of India.", true),
                            mcq("Who is called the Father of the Nation in India?", "Mahatma Gandhi", "Nehru", "Patel", "Bose"),
                            mcq("Which is the largest ocean on Earth?", "Pacific Ocean", "Indian Ocean", "Arctic Ocean", "Atlantic Ocean"),
                            mcq("How many wheels does a bicycle have?", "2", "3", "4", "1"),
                            mcq("Which planet do we live on?", "Earth", "Mars", "Moon", "Sun"),
                            mcq("Which number do we dial to call the police in India?", "100", "101", "102", "108"),
                            mcq("Which meal do we eat in the morning?", "Breakfast", "Lunch", "Dinner", "Supper"),
                            mcq("Green light on traffic signals means…", "Go", "Stop", "Wait", "Turn back"),
                            mcq("Who repairs our cars?", "Mechanic", "Doctor", "Teacher", "Farmer"),
                            tf("We should not go with strangers.", true)
                    )
            )),
            Map.entry(BAND_EXP, Map.of(
                    "Welcome", List.of(
                            mcq("What does StudyShield turn your TV time into?", "Learning time", "Sleeping time", "Arguing time", "Advertising time"),
                            mcq("Which profile setting unlocks tests for your own class?", "My kid profile (class and syllabus)", "The Wi-Fi password", "The TV remote batteries", "Screen brightness"),
                            mcq("When do StudyShield quizzes appear on the TV?", "During TV ad breaks", "Only at midnight", "Never", "After bedtime"),
                            mcq("Who can update a kid's class profile?", "A parent", "The TV", "The remote control", "Nobody"),
                            mcq("What appears once your class profile is set?", "Tests for my real class appear", "Nothing changes", "The TV stops working", "The app deletes itself"),
                            mcq("Which one of these is a fruit?", "Mango", "Carrot", "Potato", "Onion"),
                            mcq("Which number comes right after 4?", "5", "3", "6", "40"),
                            mcq("Which animal says 'meow'?", "Cat", "Dog", "Cow", "Lion"),
                            mcq("What colour is the sky on a clear day?", "Blue", "Green", "Black", "Pink"),
                            mcq("How many days are there in a week?", "7", "5", "10", "2"),
                            mcq("Which body part helps us walk?", "Legs", "Ears", "Eyes", "Nose"),
                            mcq("What is 2 + 2?", "4", "3", "5", "22"),
                            mcq("Which one of these is a vehicle?", "Bus", "Mango", "Chair", "Cloud"),
                            mcq("We read books to…", "Learn new things", "Forget everything", "Lose friends", "Break the TV"),
                            mcq("StudyShield quizzes are…", "Fun learning breaks", "Punishments", "Commercials", "Homework for parents")
                    )
            )),
            Map.entry("Class 2", class2()),
            Map.entry("Class 3", class3()),
            Map.entry("Class 4", class4()),
            Map.entry("Class 5", class5()),
            Map.entry("Class 6", class6()),
            Map.entry("Class 7", class7()),
            Map.entry("Class 8", class8()),
            Map.entry("Class 9", class9()),
            Map.entry("Class 10", class10()));

    /** Class 2 (age 7-8): two/three-digit numbers, intro multiplication, plants/animals, basic grammar. */
    private static Map<String, List<SeedQuestion>> class2() {
        return Map.of(
                "Math", List.of(
                        mcq("What is 25 + 10?", "35", "30", "45", "26"),
                        mcq("What is 40 − 15?", "25", "35", "20", "55"),
                        mcq("How much is 2 × 3?", "6", "5", "8", "9"),
                        mcq("Which number is greater: 78 or 87?", "87", "78", "Both are equal", "Cannot say"),
                        tf("100 comes just after 99.", true),
                        mcq("How many tens make 50?", "5", "50", "10", "15"),
                        mcq("What is 12 − 7?", "5", "6", "7", "4"),
                        mcq("There are 4 groups of 5 mangoes. How many mangoes in all?", "20", "9", "25", "15"),
                        mcq("Which coin value is the smallest?", "₹1", "₹2", "₹5", "₹10"),
                        mcq("Complete the pattern: 2, 4, 6, 8, ___", "10", "9", "12", "7")
                ),
                "EVS", List.of(
                        mcq("Which part of the plant takes water from the soil?", "Roots", "Leaves", "Flowers", "Fruits"),
                        mcq("Which animal lives both on land and in water?", "Frog", "Cow", "Sparrow", "Fish"),
                        mcq("Which of these is a bird that cannot fly?", "Penguin", "Crow", "Parrot", "Sparrow"),
                        tf("Plants need sunlight to grow.", true),
                        mcq("We get eggs from…", "Hen", "Cow", "Goat", "Buffalo"),
                        mcq("Which sense organ helps us know a flower smells nice?", "Nose", "Ear", "Eye", "Hand"),
                        mcq("Where do fish live?", "Water", "Trees", "Desert", "Mountains"),
                        mcq("Which one keeps our house clean?", "Broom", "Ball", "Toy", "Shoe"),
                        tf("We should wash our hands before eating.", true),
                        mcq("Which month has the fewest days?", "February", "January", "March", "April")
                ),
                "English", List.of(
                        mcq("One child, two…", "Children", "Childs", "Childes", "Child"),
                        mcq("What is the opposite of full?", "Empty", "Heavy", "Big", "Tall"),
                        mcq("Choose the right word: The bird ___ in the sky.", "flies", "fly", "flying", "flewed"),
                        tf("We use a question mark (?) at the end of a question.", true),
                        mcq("Which word rhymes with 'book'?", "Look", "Back", "Bike", "Bake"),
                        mcq("'Elephant' begins with which letter?", "E", "A", "O", "L"),
                        mcq("What is the opposite of night?", "Day", "Dark", "Sleep", "Star"),
                        mcq("Pick the naming word (noun):", "Dog", "Run", "Blue", "Slowly"),
                        mcq("Fill in the blank: T__ (toy)", "o", "a", "e", "i"),
                        tf("Names of people and places start with a capital letter.", true)
                ),
                "General Knowledge", List.of(
                        mcq("Which is the national flower of India?", "Lotus", "Rose", "Sunflower", "Marigold"),
                        mcq("How many days are there in a year?", "365", "300", "400", "360"),
                        mcq("Which animal is called the ship of the desert?", "Camel", "Horse", "Donkey", "Elephant"),
                        tf("The Indian flag has three colours.", true),
                        mcq("Which is the largest animal on Earth?", "Blue whale", "Elephant", "Giraffe", "Shark"),
                        mcq("What do we call a place where we borrow books?", "Library", "Hospital", "Market", "Airport"),
                        mcq("Which festival is the festival of colours?", "Holi", "Diwali", "Eid", "Pongal"),
                        mcq("Who delivers letters to our homes?", "Postman", "Farmer", "Dentist", "Tailor"),
                        mcq("The sun rises in which direction?", "East", "West", "North", "South"),
                        tf("A rainbow has seven colours.", true)
                ));
    }

    /** Class 3 (age 8-9): times tables, division, money, habitats, tenses. */
    private static Map<String, List<SeedQuestion>> class3() {
        return Map.of(
                "Math", List.of(
                        mcq("What is 6 × 4?", "24", "20", "18", "26"),
                        mcq("What is 45 + 38?", "83", "73", "93", "82"),
                        mcq("What is 18 ÷ 3?", "6", "5", "7", "9"),
                        tf("Any number multiplied by 1 stays the same.", true),
                        mcq("Which number is odd?", "27", "20", "36", "48"),
                        mcq("How many 100s are there in 500?", "5", "50", "500", "15"),
                        mcq("Ravi has ₹50 and spends ₹20. How much is left?", "₹30", "₹20", "₹70", "₹25"),
                        mcq("What is half of 16?", "8", "6", "4", "12"),
                        mcq("Complete the pattern: 5, 10, 15, 20, ___", "25", "22", "24", "30"),
                        tf("0 multiplied by any number gives 0.", true)
                ),
                "EVS", List.of(
                        mcq("Which body organ pumps blood?", "Heart", "Lungs", "Stomach", "Brain"),
                        mcq("Animals that eat only plants are called…", "Herbivores", "Carnivores", "Omnivores", "Predators"),
                        mcq("Why do birds build nests?", "To lay eggs and raise chicks", "To sleep at night only", "To store water", "To hide from rain forever"),
                        tf("The Moon gets its light from the Sun.", true),
                        mcq("Which food group gives us energy?", "Carbohydrates like rice", "Vitamins only", "Water only", "Salt"),
                        mcq("Where does rubber come from?", "Rubber tree", "Rocks", "Sea shells", "Sand"),
                        mcq("Which is the longest river in India?", "Ganga", "Yamuna", "Godavari", "Narmada"),
                        mcq("What should we do with waste?", "Put it in a dustbin", "Throw it on the road", "Burn it in the room", "Push it under the bed"),
                        tf("We should never play with fire.", true),
                        mcq("A place where animals are protected from hunters is a…", "Sanctuary", "Stadium", "Station", "School")
                ),
                "English", List.of(
                        mcq("Yesterday I ___ to the market.", "went", "go", "gone", "going"),
                        mcq("Choose the correct spelling:", "Friend", "Freind", "Frind", "Frienn"),
                        mcq("The opposite of 'ancient' is…", "Modern", "Old", "Antique", "Past"),
                        tf("'She' and 'they' are pronouns.", true),
                        mcq("One tooth, many…", "Teeth", "Tooths", "Toothes", "Teeths"),
                        mcq("Which word rhymes with 'night'?", "Light", "Noon", "Day", "Dark"),
                        mcq("Pick the action word (verb):", "Swim", "Table", "Green", "Happy"),
                        mcq("Fill in: ___ umbrella is blue.", "Her", "She", "Herself", "Hers"),
                        tf("Every sentence must begin with a capital letter.", true),
                        mcq("Which sentence is correct?", "I am reading a book.", "I reading a book am.", "Reading I a book am.", "Am I reading book a.")
                ),
                "General Knowledge", List.of(
                        mcq("Who was the first President of India?", "Dr. Rajendra Prasad", "Dr. S. Radhakrishnan", "Jawaharlal Nehru", "Dr. A.P.J. Abdul Kalam"),
                        mcq("How many continents are there on Earth?", "7", "5", "6", "8"),
                        mcq("Which planet is closest to the Sun?", "Mercury", "Earth", "Venus", "Mars"),
                        tf("The cheetah is the fastest land animal.", true),
                        mcq("Which instrument has black and white keys?", "Piano", "Guitar", "Flute", "Drum"),
                        mcq("What is the capital of Maharashtra?", "Mumbai", "Nagpur", "Pune", "Delhi"),
                        mcq("Which gas do we breathe in to live?", "Oxygen", "Carbon dioxide", "Helium", "Nitrogen"),
                        mcq("A doctor who treats teeth is a…", "Dentist", "Surgeon", "Optician", "Veterinarian"),
                        mcq("Which is the smallest state of India by area?", "Goa", "Kerala", "Sikkim", "Tripura"),
                        tf("India got independence in 1947.", true)
                ));
    }

    /** Class 4 (age 9-10): factors, fractions intro, measurement, states of matter. */
    private static Map<String, List<SeedQuestion>> class4() {
        return Map.of(
                "Math", List.of(
                        mcq("What is 7 × 8?", "56", "48", "54", "63"),
                        mcq("Which number is a factor of 12?", "3", "5", "7", "8"),
                        mcq("What is 144 ÷ 12?", "12", "11", "14", "24"),
                        mcq("How many minutes are in 2 hours?", "120", "100", "60", "200"),
                        tf("In 3/4, the number 4 is called the denominator.", true),
                        mcq("What is the place value of 7 in 4,752?", "700", "70", "7", "7000"),
                        mcq("A rectangle is 6 cm long and 4 cm wide. What is its perimeter?", "20 cm", "24 cm", "10 cm", "12 cm"),
                        mcq("Round 46 to the nearest ten.", "50", "40", "45", "100"),
                        mcq("What is 2 × 2 × 2?", "8", "6", "4", "16"),
                        tf("A square has four equal sides.", true)
                ),
                "EVS", List.of(
                        mcq("Water turns into ice by…", "Freezing", "Boiling", "Melting", "Evaporating"),
                        mcq("Which of these is a renewable resource?", "Sunlight", "Coal", "Petrol", "Diesel"),
                        mcq("The process by which plants make food is…", "Photosynthesis", "Digestion", "Respiration", "Germination"),
                        tf("Air has weight.", true),
                        mcq("Which organ helps us breathe?", "Lungs", "Liver", "Kidney", "Heart"),
                        mcq("An animal that eats both plants and other animals is an…", "Omnivore", "Herbivore", "Carnivore", "Producer"),
                        mcq("Which state of matter takes the shape of its container?", "Liquid", "Solid", "Both solid and liquid", "None"),
                        mcq("Earthworms help farmers by…", "Making soil loose and fertile", "Eating crops", "Drinking water", "Singing"),
                        mcq("Which of these is NOT a sense organ?", "Hair", "Eye", "Ear", "Skin"),
                        tf("We should drink at least 6–8 glasses of water a day.", true)
                ),
                "English", List.of(
                        mcq("Choose the correct word: The cat sat ___ the mat.", "on", "in", "at", "of"),
                        mcq("The opposite of 'accept' is…", "Reject", "Except", "Agree", "Allow"),
                        mcq("Which word is spelled correctly?", "Beautiful", "Beutiful", "Beautifull", "Butiful"),
                        tf("An adjective describes a noun.", true),
                        mcq("She ___ her homework every day.", "does", "do", "doing", "done"),
                        mcq("Pick the collective noun: a ___ of sheep", "flock", "bunch", "team", "class"),
                        mcq("What is the past tense of 'eat'?", "ate", "eated", "eaten", "eating"),
                        mcq("Which sentence uses punctuation correctly?", "Where are you going?", "Where are you going", "where are you going?", "Where are you going!"),
                        mcq("Fill in: The ___ boy won the race.", "brave", "bravely", "braveness", "braving"),
                        tf("A paragraph usually starts on a new line.", true)
                ),
                "General Knowledge", List.of(
                        mcq("Who wrote the national anthem of India?", "Rabindranath Tagore", "Bankim Chandra Chatterjee", "Sarojini Naidu", "Subhash Chandra Bose"),
                        mcq("Which is the highest mountain in the world?", "Mount Everest", "K2", "Kanchenjunga", "Nanda Devi"),
                        tf("The peacock is the national bird of India.", true),
                        mcq("Which festival celebrates the birth of Jesus Christ?", "Christmas", "Easter", "Diwali", "Onam"),
                        mcq("What does a thermometer measure?", "Temperature", "Speed", "Weight", "Length"),
                        mcq("Which sport uses a bat, ball and wickets?", "Cricket", "Football", "Hockey", "Tennis"),
                        mcq("Which ocean is the largest?", "Pacific Ocean", "Atlantic Ocean", "Indian Ocean", "Arctic Ocean"),
                        mcq("The Great Wall is in which country?", "China", "India", "Japan", "Egypt"),
                        mcq("Which bird is a symbol of peace?", "Dove", "Crow", "Eagle", "Owl"),
                        tf("The Sun is a star.", true)
                ));
    }


    /** Class 5 (age 10-11): decimals, percentages intro, geometry basics, human body. */
    private static Map<String, List<SeedQuestion>> class5() {
        return Map.of(
                "Math", List.of(
                        mcq("What is 0.5 + 0.25?", "0.75", "0.7", "0.30", "1.25"),
                        mcq("What is 25% of 200?", "50", "25", "75", "100"),
                        mcq("Which fraction equals 0.5?", "1/2", "1/4", "1/5", "2/5"),
                        mcq("The area of a square with side 5 cm is…", "25 sq cm", "20 sq cm", "10 sq cm", "15 sq cm"),
                        tf("All angles of a triangle add up to 180 degrees.", true),
                        mcq("What is 3/4 of 20?", "15", "10", "12", "16"),
                        mcq("Convert 3 km into metres:", "3000 m", "300 m", "30 m", "30000 m"),
                        mcq("Which decimal is greater: 0.7 or 0.65?", "0.7", "0.65", "They are equal", "Cannot compare"),
                        mcq("The LCM of 4 and 6 is…", "12", "24", "6", "2"),
                        mcq("What is 999 + 1?", "1000", "990", "1001", "900")
                ),
                "EVS", List.of(
                        mcq("Which organ filters blood?", "Kidney", "Lungs", "Stomach", "Brain"),
                        tf("The heart has four chambers.", true),
                        mcq("Solar energy comes from…", "The Sun", "Wind", "Water", "Coal"),
                        mcq("Which planet is known as the Red Planet?", "Mars", "Venus", "Jupiter", "Saturn"),
                        mcq("Deforestation means…", "Cutting down forests", "Planting trees", "Protecting animals", "Cleaning rivers"),
                        mcq("Which food is rich in protein?", "Pulses", "Sugar", "Oil", "Salt"),
                        mcq("The force that pulls objects towards Earth is…", "Gravity", "Friction", "Magnetism", "Electricity"),
                        mcq("Which vitamin do we get from sunlight?", "Vitamin D", "Vitamin A", "Vitamin C", "Vitamin B"),
                        tf("Recycling paper helps save trees.", true),
                        mcq("How many bones are there in an adult human body?", "206", "306", "106", "256")
                ),
                "English", List.of(
                        mcq("Choose the synonym of 'begin':", "Start", "Finish", "Middle", "End"),
                        mcq("The antonym of 'victory' is…", "Defeat", "Win", "Success", "Triumph"),
                        mcq("Which sentence is in the future tense?", "I will visit Goa.", "I visited Goa.", "I visit Goa.", "I am visiting Goa."),
                        tf("'Quickly' is an adverb.", true),
                        mcq("Identify the conjunction: Ram and Shyam are friends.", "and", "Ram", "are", "friends"),
                        mcq("One mouse, many…", "mice", "mouses", "mices", "mousees"),
                        mcq("Choose the correct spelling:", "Necessary", "Neccessary", "Necesary", "Necessery"),
                        mcq("A person who writes books is an…", "author", "editor", "actor", "artist"),
                        mcq("Fill in: If I ___ rich, I would help everyone.", "were", "am", "is", "be"),
                        tf("We use commas to separate items in a list.", true)
                ),
                "General Knowledge", List.of(
                        mcq("Who invented the telephone?", "Alexander Graham Bell", "Thomas Edison", "Isaac Newton", "Guglielmo Marconi"),
                        mcq("Which is the longest river in the world?", "Nile", "Amazon", "Ganga", "Yangtze"),
                        tf("The currency of Japan is the Yen.", true),
                        mcq("Which gas makes up most of Earth's atmosphere?", "Nitrogen", "Oxygen", "Carbon dioxide", "Hydrogen"),
                        mcq("The Olympic Games happen every…", "4 years", "2 years", "year", "5 years"),
                        mcq("Who was the first Indian woman in space?", "Kalpana Chawla", "Sunita Williams", "Indira Gandhi", "Sania Mirza"),
                        mcq("Which desert is the largest hot desert?", "Sahara", "Thar", "Gobi", "Kalahari"),
                        mcq("How many players are on a football team on the field?", "11", "9", "10", "12"),
                        mcq("Which metal is liquid at room temperature?", "Mercury", "Iron", "Gold", "Silver"),
                        tf("The Taj Mahal is in Agra.", true)
                ));
    }

    /** Class 6 (age 11-12): integers, algebra intro, ratio; science-leaning EVS. */
    private static Map<String, List<SeedQuestion>> class6() {
        return Map.of(
                "Math", List.of(
                        mcq("Which number is smaller than −3?", "−5", "−1", "0", "2"),
                        mcq("What is (−4) + 7?", "3", "−3", "11", "−11"),
                        mcq("If x + 5 = 12, then x =", "7", "17", "−7", "60"),
                        mcq("Simplify the ratio 12 : 18:", "2 : 3", "3 : 2", "6 : 9", "4 : 6"),
                        tf("Every natural number is a whole number.", true),
                        mcq("What is 15% of 400?", "60", "45", "15", "600"),
                        mcq("The HCF of 12 and 18 is…", "6", "3", "36", "12"),
                        mcq("Perimeter of a square with side 7 cm:", "28 cm", "49 cm", "14 cm", "21 cm"),
                        mcq("What is 0.2 × 0.3?", "0.06", "0.6", "6", "0.006"),
                        mcq("Which is a prime number?", "13", "21", "27", "33")
                ),
                "EVS", List.of(
                        mcq("Which part of the cell controls its activities?", "Nucleus", "Cell wall", "Cytoplasm", "Vacuole"),
                        tf("Light travels in a straight line.", true),
                        mcq("Separating grain from stalk by beating is called…", "Threshing", "Winnowing", "Sieving", "Filtering"),
                        mcq("Which of these is a conductor of electricity?", "Copper", "Rubber", "Wood", "Plastic"),
                        mcq("The change of water vapour into liquid is…", "Condensation", "Evaporation", "Freezing", "Melting"),
                        mcq("Which nutrient is the main source of energy?", "Carbohydrates", "Proteins", "Vitamins", "Minerals"),
                        mcq("Motion of a pendulum is…", "Periodic motion", "Linear motion", "Random motion", "No motion"),
                        tf("Magnets always have two poles.", true),
                        mcq("Fibres obtained from plants and animals are called…", "Natural fibres", "Synthetic fibres", "Plastic fibres", "Metal fibres"),
                        mcq("Which gas do plants absorb during photosynthesis?", "Carbon dioxide", "Oxygen", "Nitrogen", "Hydrogen")
                ),
                "English", List.of(
                        mcq("Choose the correct form: He has been working ___ morning.", "since", "for", "from", "at"),
                        mcq("The synonym of 'generous' is…", "Kind", "Greedy", "Selfish", "Rude"),
                        mcq("Identify the adverb: She sings beautifully.", "beautifully", "she", "sings", "none"),
                        tf("A metaphor compares two things without using 'like' or 'as'.", true),
                        mcq("Change to passive voice: 'Ria wrote a letter.' →", "A letter was written by Ria.", "A letter wrote by Ria.", "Ria was written a letter.", "A letter is writing Ria."),
                        mcq("Which is a compound word?", "Sunflower", "Running", "Happily", "Kindness"),
                        mcq("The plural of 'leaf' is…", "leaves", "leafs", "leafes", "leaf"),
                        mcq("Pick the correct sentence:", "Neither of them came.", "Neither them came.", "Neither of they came.", "Neither of them come."),
                        mcq("A group of lions is called a…", "pride", "pack", "herd", "school"),
                        tf("Direct speech quotes the exact words spoken.", true)
                ),
                "General Knowledge", List.of(
                        mcq("Which city is the capital of India?", "New Delhi", "Mumbai", "Kolkata", "Chennai"),
                        mcq("The study of stars and planets is called…", "Astronomy", "Geology", "Biology", "Chemistry"),
                        tf("Mount Everest lies between Nepal and China.", true),
                        mcq("Who is known as the Missile Man of India?", "Dr. A.P.J. Abdul Kalam", "Homi Bhabha", "Vikram Sarabhai", "C.V. Raman"),
                        mcq("Which is the smallest continent?", "Australia", "Europe", "Antarctica", "South America"),
                        mcq("The ancient Olympic Games were held in…", "Greece", "Rome", "Egypt", "India"),
                        mcq("Which instrument measures atmospheric pressure?", "Barometer", "Thermometer", "Speedometer", "Altimeter"),
                        mcq("Silk is obtained from…", "Silkworm", "Sheep", "Goat", "Camel"),
                        mcq("How many main directions are there?", "4", "2", "6", "8"),
                        tf("Photosynthesis happens mostly in leaves.", true)
                ));
    }

    /** Class 7 (age 12-13): fraction ops, simple equations, heat/light science. */
    private static Map<String, List<SeedQuestion>> class7() {
        return Map.of(
                "Math", List.of(
                        mcq("What is 2/3 + 1/6?", "5/6", "3/9", "1/2", "4/6"),
                        mcq("If 3x = 27, then x =", "9", "24", "81", "8"),
                        mcq("Two angles that add up to 90° are called…", "Complementary", "Supplementary", "Vertical", "Reflex"),
                        mcq("What is (−5) × (−4)?", "20", "−20", "9", "−9"),
                        tf("The product of two negative numbers is positive.", true),
                        mcq("Simple interest on ₹1000 at 10% per year for 2 years is…", "₹200", "₹100", "₹210", "₹20"),
                        mcq("Which fraction is largest: 1/2, 3/5, 2/3?", "2/3", "1/2", "3/5", "All equal"),
                        mcq("Sum of interior angles of a triangle is…", "180°", "360°", "270°", "90°"),
                        mcq("What is 25% expressed as a fraction?", "1/4", "1/5", "1/2", "1/25"),
                        mcq("If y − 7 = −2, then y =", "5", "−9", "9", "−5")
                ),
                "EVS", List.of(
                        mcq("Heat transfer in solids happens mainly by…", "Conduction", "Convection", "Radiation", "Evaporation"),
                        tf("Sound cannot travel through a vacuum.", true),
                        mcq("Which acid is present in the stomach?", "Hydrochloric acid", "Sulphuric acid", "Nitric acid", "Citric acid"),
                        mcq("The process where a caterpillar becomes a butterfly is…", "Metamorphosis", "Pollination", "Germination", "Migration"),
                        mcq("Which blood cells fight infection?", "White blood cells", "Red blood cells", "Platelets", "Plasma"),
                        mcq("A speed of 60 km/h means travelling 60 km in…", "1 hour", "60 hours", "6 minutes", "half an hour"),
                        mcq("Which of these is a chemical change?", "Rusting of iron", "Melting of ice", "Breaking glass", "Boiling water"),
                        mcq("Rainbows are formed by…", "Dispersion of light", "Reflection only", "Absorption of light", "Noise"),
                        tf("Respiration releases energy from food.", true),
                        mcq("Which organ produces insulin?", "Pancreas", "Liver", "Heart", "Kidney")
                ),
                "English", List.of(
                        mcq("Choose the correct article: He is ___ honest man.", "an", "a", "the", "no article"),
                        mcq("The synonym of 'courage' is…", "Bravery", "Fear", "Anger", "Joy"),
                        mcq("Identify the tense: They had finished the work.", "Past perfect", "Present perfect", "Future perfect", "Simple past"),
                        tf("'Their', 'there' and 'they're' sound the same but mean different things.", true),
                        mcq("Which is a superlative adjective?", "best", "better", "good", "well"),
                        mcq("Rewrite correctly: 'he said me nothing'", "He told me nothing.", "He said me nothing.", "He nothing said me.", "Me he said nothing."),
                        mcq("The antonym of 'transparent' is…", "Opaque", "Clear", "Visible", "Shiny"),
                        mcq("Which sentence contains a simile?", "As brave as a lion", "Brave lion roars", "The lion is brave", "Bravery of lions"),
                        mcq("One wolf, many…", "wolves", "wolfs", "wolfes", "wolf"),
                        tf("A biography is written about someone by another person.", true)
                ),
                "General Knowledge", List.of(
                        mcq("Who painted the Mona Lisa?", "Leonardo da Vinci", "Picasso", "Van Gogh", "Michelangelo"),
                        mcq("Which is the deepest ocean trench?", "Mariana Trench", "Java Trench", "Puerto Rico Trench", "Tonga Trench"),
                        tf("The UN headquarters is in New York.", true),
                        mcq("Which Indian state has the longest coastline?", "Gujarat", "Kerala", "Tamil Nadu", "Maharashtra"),
                        mcq("The Harappan civilization was discovered in the year…", "1921", "1857", "1947", "1750"),
                        mcq("Which vitamin deficiency causes night blindness?", "Vitamin A", "Vitamin C", "Vitamin D", "Vitamin K"),
                        mcq("The currency of the USA is…", "Dollar", "Euro", "Pound", "Yen"),
                        mcq("Which planet has prominent rings?", "Saturn", "Mars", "Venus", "Mercury"),
                        mcq("Who founded the Indian National Army (INA)?", "Subhash Chandra Bose", "Bhagat Singh", "Nehru", "Patel"),
                        tf("The brain is protected by the skull.", true)
                ));
    }

    /** Class 8 (age 13-14): exponents, linear equations, force/pressure, civics. */
    private static Map<String, List<SeedQuestion>> class8() {
        return Map.of(
                "Math", List.of(
                        mcq("What is 2³ × 2² ?", "32", "64", "16", "4"),
                        mcq("The square root of 144 is…", "12", "14", "16", "24"),
                        mcq("Solve: 2x + 5 = 15", "x = 5", "x = 10", "x = −5", "x = 7.5"),
                        tf("The cube of 3 is 27.", true),
                        mcq("A number divisible by both 2 and 3 is also divisible by…", "6", "5", "8", "9"),
                        mcq("What is 15% of ₹800?", "₹120", "₹80", "₹150", "₹12"),
                        mcq("The value of (−1)^101 is…", "−1", "1", "0", "101"),
                        mcq("Area of a circle with radius 7 cm (π = 22/7):", "154 sq cm", "44 sq cm", "49 sq cm", "22 sq cm"),
                        mcq("Factorise: x² − 9", "(x+3)(x−3)", "(x+9)(x−1)", "(x−3)²", "(x+3)²"),
                        mcq("Which is irrational?", "√2", "0.25", "3/4", "−7")
                ),
                "EVS", List.of(
                        mcq("The SI unit of force is…", "Newton", "Joule", "Watt", "Pascal"),
                        tf("Friction always opposes motion.", true),
                        mcq("Which microorganism causes malaria?", "Protozoa", "Bacteria", "Virus", "Fungus"),
                        mcq("Petrol, diesel and coal are examples of…", "Fossil fuels", "Renewable fuels", "Biofuels", "Nuclear fuels"),
                        mcq("The pressure exerted by air is called…", "Atmospheric pressure", "Water pressure", "Friction", "Tension"),
                        mcq("Which crop needs standing water to grow?", "Paddy", "Wheat", "Cotton", "Jute"),
                        mcq("Sound is produced when objects…", "Vibrate", "Freeze", "Burn", "Shine"),
                        mcq("Robert Hooke first observed cells in…", "Cork", "Blood", "Leaf", "Root"),
                        tf("Metals are good conductors of heat.", true),
                        mcq("Which of these is NOT a fossil fuel?", "Uranium", "Coal", "Petroleum", "Natural gas")
                ),
                "English", List.of(
                        mcq("Passive voice of 'They built the bridge.':", "The bridge was built by them.", "The bridge is built by them.", "The bridge built them.", "They are built the bridge."),
                        mcq("The synonym of 'abundant' is…", "Plentiful", "Scarce", "Rare", "Empty"),
                        mcq("Identify the modal verb: You should exercise daily.", "should", "exercise", "daily", "you"),
                        tf("An idiom's meaning cannot be understood from its individual words.", true),
                        mcq("Choose the correctly punctuated sentence:", "It's raining, isn't it?", "Its raining isnt it?", "It's raining isn't it", "its raining, isn't it?"),
                        mcq("Which word is a preposition?", "beneath", "quickly", "happy", "jump"),
                        mcq("The antonym of 'artificial' is…", "Natural", "Fake", "Synthetic", "Plastic"),
                        mcq("Reported speech: He said, \"I am tired.\" →", "He said that he was tired.", "He says he is tired.", "He said I am tired.", "He said that I am tired."),
                        mcq("Which is an abstract noun?", "honesty", "table", "river", "dog"),
                        tf("Editing means checking for grammar, spelling and clarity.", true)
                ),
                "General Knowledge", List.of(
                        mcq("The original Indian Constitution contained approximately how many Articles?", "395", "448", "250", "500"),
                        mcq("The Revolt of 1857 started at…", "Meerut", "Delhi", "Kanpur", "Jhansi"),
                        tf("The World Wide Web was invented by Tim Berners-Lee.", true),
                        mcq("Which is the fastest land animal?", "Cheetah", "Lion", "Horse", "Leopard"),
                        mcq("The headquarters of ISRO is in…", "Bengaluru", "Chennai", "Mumbai", "Hyderabad"),
                        mcq("Who was the first woman Prime Minister of India?", "Indira Gandhi", "Sarojini Naidu", "Pratibha Patil", "Sonia Gandhi"),
                        mcq("Which element has the symbol 'Fe'?", "Iron", "Fluorine", "Francium", "Lead"),
                        mcq("Greenhouse effect leads to…", "Global warming", "Cooling of Earth", "More rainfall only", "Earthquakes"),
                        mcq("The Battle of Plassey was fought in…", "1757", "1857", "1764", "1526"),
                        tf("Renewable energy sources never run out.", true)
                ));
    }

    /** Class 9 (age 14-15): polynomials, motion, atoms, literature-level English. */
    private static Map<String, List<SeedQuestion>> class9() {
        return Map.of(
                "Math", List.of(
                        mcq("Degree of the polynomial 4x³ − 2x + 7 is…", "3", "2", "1", "7"),
                        mcq("Expand (x + 2)(x + 3):", "x² + 5x + 6", "x² + 6x + 5", "x² + 5", "x² + 6"),
                        mcq("The distance of point (3, 4) from origin is…", "5", "7", "4", "3"),
                        tf("Two triangles are similar if their corresponding angles are equal.", true),
                        mcq("Zero of the polynomial p(x) = x − 5 is…", "5", "−5", "0", "1"),
                        mcq("Volume of a cylinder with radius r and height h is…", "πr²h", "2πrh", "πrh²", "4πr²"),
                        mcq("If two lines intersect, vertically opposite angles are…", "Equal", "Supplementary", "Complementary", "Unequal"),
                        mcq("Mean of 4, 6, 8, 10 is…", "7", "6", "8", "28"),
                        mcq("Which of these is irrational?", "π", "22/7", "0.5", "−7"),
                        tf("Every point on the number line represents a unique real number.", true)
                ),
                "EVS", List.of(
                        mcq("Newton's second law states F = …", "ma", "mv", "m/a", "m+v"),
                        tf("Acceleration due to gravity on Earth is about 9.8 m/s².", true),
                        mcq("The basic unit of matter is the…", "Atom", "Cell", "Tissue", "Organ"),
                        mcq("Which tissue transports water in plants?", "Xylem", "Phloem", "Parenchyma", "Cambium"),
                        mcq("The chemical formula of water is…", "H₂O", "CO₂", "O₂", "H₂O₂"),
                        mcq("Distance covered per unit time is…", "Speed", "Acceleration", "Force", "Work"),
                        mcq("Which disease is caused by a virus?", "Influenza", "Typhoid", "TB", "Cholera"),
                        mcq("Energy stored in food is measured in…", "Calories", "Metres", "Newtons", "Volts"),
                        mcq("Which gas shields Earth from UV radiation?", "Ozone", "Oxygen", "Nitrogen", "Helium"),
                        tf("Work done is zero when force and displacement are perpendicular.", true)
                ),
                "English", List.of(
                        mcq("'Break a leg' is an example of…", "Idiom", "Simile", "Metaphor", "Alliteration"),
                        mcq("Collective noun for bees:", "swarm", "pride", "flock", "herd"),
                        mcq("The literary device in 'the wind whispered' is…", "Personification", "Hyperbole", "Simile", "Irony"),
                        tf("An autobiography is written by the person themselves.", true),
                        mcq("Synonym of 'diligent':", "Hardworking", "Lazy", "Careless", "Rude"),
                        mcq("Identify the clause type: 'I know that he is honest.'", "Noun clause", "Adjective clause", "Adverb clause", "Main clause only"),
                        mcq("The antonym of 'scarce' is…", "Plentiful", "Limited", "Rare", "Sparse"),
                        mcq("Which sentence is grammatically correct?", "Between you and me, this is fine.", "Between you and I, this is fine.", "Between I and you, this is fine.", "Between us and I, this is fine."),
                        mcq("A traditional haiku has how many syllables in total (5-7-5)?", "17", "12", "14", "20"),
                        tf("Active voice makes writing more direct than passive voice.", true)
                ),
                "General Knowledge", List.of(
                        mcq("Who discovered penicillin?", "Alexander Fleming", "Louis Pasteur", "Edward Jenner", "Robert Koch"),
                        mcq("The Indian Parliament consists of Lok Sabha, Rajya Sabha and…", "The President", "The Supreme Court", "The Cabinet", "The Election Commission"),
                        tf("Chandrayaan-3 landed near the Moon's south pole in 2023.", true),
                        mcq("Which Mughal emperor built the Taj Mahal?", "Shah Jahan", "Akbar", "Aurangzeb", "Humayun"),
                        mcq("The Richter scale measures…", "Earthquakes", "Wind speed", "Temperature", "Ocean depth"),
                        mcq("Who wrote 'Discovery of India'?", "Jawaharlal Nehru", "Mahatma Gandhi", "Rabindranath Tagore", "Ambedkar"),
                        mcq("Which country gifted the Statue of Liberty to the USA?", "France", "Britain", "Spain", "Italy"),
                        mcq("The Tropic of Cancer passes through how many Indian states?", "8", "6", "10", "5"),
                        mcq("Blockchain technology was first applied to…", "Cryptocurrency", "Healthcare", "Education", "Farming"),
                        tf("Democracy means government by the people.", true)
                ));
    }

    /** Class 10 (age 15-16, boards level): quadratic equations, trigonometry, electricity, life processes. */
    private static Map<String, List<SeedQuestion>> class10() {
        return Map.of(
                "Math", List.of(
                        mcq("The discriminant of ax² + bx + c is…", "b² − 4ac", "b² + 4ac", "4ac − b²", "2a + b"),
                        mcq("sin 30° equals…", "1/2", "1", "√3/2", "0"),
                        mcq("If roots of x² − 5x + 6 = 0 are α and β, then αβ =", "6", "5", "−6", "−5"),
                        tf("tan 45° = 1.", true),
                        mcq("The 10th term of the AP 2, 5, 8, … is…", "29", "26", "32", "27"),
                        mcq("Distance between points (0,0) and (6,8) is…", "10", "14", "2", "48"),
                        mcq("A tangent to a circle touches it at…", "Exactly one point", "Two points", "Three points", "No point"),
                        mcq("Curved surface area of a cone is…", "πrl", "2πrh", "πr²h", "4πr²"),
                        mcq("Probability of getting a head in one coin toss is…", "1/2", "1", "0", "1/4"),
                        mcq("The zeros of x² − 4 are…", "2 and −2", "4 and −4", "0 and 4", "2 and 4")
                ),
                "EVS", List.of(
                        mcq("The SI unit of electric current is…", "Ampere", "Volt", "Ohm", "Watt"),
                        tf("Ohm's law relates voltage, current and resistance.", true),
                        mcq("Which reaction absorbs heat?", "Endothermic", "Exothermic", "Neutralisation", "Combustion"),
                        mcq("Ethanol can be produced by fermentation of…", "Sugar", "Salt", "Starch alone", "Fat"),
                        mcq("The male reproductive part of a flower is…", "Stamen", "Pistil", "Sepal", "Petal"),
                        mcq("Which lens is used to correct myopia?", "Concave lens", "Convex lens", "Cylindrical lens", "Bifocal only"),
                        mcq("Genetic material in humans is…", "DNA", "RNA only", "Protein", "Lipid"),
                        mcq("The power of a lens is measured in…", "Dioptres", "Watts", "Newtons", "Volts"),
                        mcq("Decomposers include…", "Bacteria and fungi", "Deer and goats", "Fish and snakes", "Eagles and hawks"),
                        tf("AC current changes direction periodically.", true)
                ),
                "English", List.of(
                        mcq("Choose the correct usage: Neither the teacher nor the students ___ ready.", "were", "was", "is", "has"),
                        mcq("The figure of speech in 'death lays its icy hands on kings' is…", "Personification", "Simile", "Metaphor", "Onomatopoeia"),
                        tf("'Whom' is used as the object of a verb or preposition.", true),
                        mcq("Antonym of 'benevolent':", "Malevolent", "Generous", "Kindly", "Gracious"),
                        mcq("Find the error: 'One of my friend lives abroad.'", "'friend' should be 'friends'", "no error", "'lives' should be 'live'", "'of' should be 'off'"),
                        mcq("Synonym of 'ephemeral':", "Short-lived", "Eternal", "Powerful", "Hidden"),
                        mcq("Which is a complex sentence?", "Although it rained, we played.", "It rained and we played.", "We played; it rained.", "It rained. We played."),
                        mcq("The tone of a formal complaint letter should be…", "Polite and factual", "Rude and angry", "Joking", "Poetic"),
                        mcq("'To kill two birds with one stone' means…", "Achieve two results with one action", "Be cruel to animals", "Miss twice", "Try hard"),
                        tf("Summary writing should avoid personal opinions.", true)
                ),
                "General Knowledge", List.of(
                        mcq("The Preamble declares India a sovereign, socialist, secular and…", "Democratic republic", "Monarchy", "Confederation", "Colony"),
                        mcq("Fundamental Duties were added to the Constitution by which amendment?", "42nd Amendment", "1st Amendment", "73rd Amendment", "101st Amendment"),
                        tf("GST was introduced in India in 2017.", true),
                        mcq("Who is the head of the Indian government?", "Prime Minister", "President", "Chief Justice", "Speaker"),
                        mcq("The Paris Agreement deals with…", "Climate change", "Trade tariffs", "Space exploration", "Internet laws"),
                        mcq("Which organisation publishes the Human Development Index?", "UNDP", "WHO", "IMF", "World Bank"),
                        mcq("The first Indian satellite was…", "Aryabhata", "INSAT-1A", "Rohini", "Bhaskara"),
                        mcq("Right to Education covers children aged…", "6 to 14 years", "5 to 15 years", "3 to 18 years", "8 to 16 years"),
                        mcq("UPI is developed and operated by…", "NPCI", "SEBI", "TRAI", "IRDAI"),
                        tf("Voting age in India is 18 years.", true)
                ));
    }

    /**
     * Real fallback questions used to top up any class without a curated band, so a
     * session never starts empty (issue #1 acceptance criteria).
     */
    public static final List<SeedQuestion> FALLBACK_BANK = List.of(
            mcq("We smell with our…", "Nose", "Eyes", "Ears", "Hands"),
            mcq("What is 2 + 2?", "4", "3", "5", "22"),
            mcq("The sun rises in the…", "East", "West", "North", "South"),
            mcq("A cat says…", "Meow", "Moo", "Quack", "Bow-wow"),
            mcq("How many sides does a triangle have?", "3", "2", "4", "5"),
            mcq("Red light means…", "Stop", "Go", "Jump", "Sing"),
            mcq("Which animal gives us milk?", "Cow", "Dog", "Hen", "Cat"),
            mcq("We see with our…", "Eyes", "Ears", "Nose", "Feet"),
            tf("Fire is hot.", true),
            mcq("Which shape is a ball?", "Circle", "Square", "Triangle", "Star")
    );
}
