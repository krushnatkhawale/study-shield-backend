package com.studyshield.studyshield.content.seed;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Curated seed question bank, age/class appropriate per band (issue #1).
 * <p>
 * Two bands are seeded today — Sr KG (age 4-5) and Class 1 (age 6-7) — four subjects each,
 * 15 questions per subject (120 total). Questions mix SINGLE_CHOICE and TRUE_FALSE only,
 * matching what young children can read or follow when read aloud.
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

    /** Maps a normalized class name to its band key, or null when no curated bank exists. */
    public static String bandForClassName(String normalizedClassName) {
        if (normalizedClassName == null) return null;
        String t = normalizedClassName.trim().toLowerCase(Locale.ROOT);
        if (t.contains("sr") || t.contains("senior") || t.contains("ukg")) return BAND_SR_KG;
        if (t.equals("class 1") || t.equals("grade 1") || t.equals("std 1") || t.equals("1")) return BAND_CLASS_1;
        if (t.equals("exp") || t.equals("experimental") || t.equals("promo")) return BAND_EXP;
        return null;
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

    public static final Map<String, Map<String, List<SeedQuestion>>> BANK = Map.of(
            BAND_SR_KG, Map.of(
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
            ),
            BAND_CLASS_1, Map.of(
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
            ),
            BAND_EXP, Map.of(
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
            ));

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
