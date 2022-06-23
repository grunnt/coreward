package dev.basvs.coreward.console;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Therapist {

  private String name = null;
  private HashMap<String, List<String>> replies = new HashMap<>();
  private List<String> questionResponses;
  private List<String> defaultResponses;
  private Random random = new Random();

  public Therapist() {
    List<String> greetings = Arrays.asList("Hello!", "Hi", "Hello", "Nice to meet you");
    replies.put(normalize("hi"), greetings);
    replies.put(normalize("hello"), greetings);
    replies.put(normalize("hi"), greetings);
    replies.put(normalize("hi"), greetings);
    List<String> goodbyes = Arrays.asList("Farewell", "Bye", "Goodbye", "Cya", "See you later");
    replies.put(normalize("bye"), goodbyes);
    replies.put(normalize("goodbye"), goodbyes);
    replies.put(normalize("farewell"), goodbyes);
    replies.put(normalize("cya"), goodbyes);
    replies.put(normalize("see you later"), goodbyes);
    List<String> whyUnsure = Arrays.asList("What makes you unsure?", "Why don't you know?",
        "Tell my why you don't know.");
    replies.put(normalize("I dont know"), whyUnsure);
    replies.put(normalize("Idk"), whyUnsure);
    replies.put(normalize("Im not sure"), whyUnsure);
    replies.put(normalize("Im unsure"), whyUnsure);
    List<String> whyDisagree = Arrays.asList("Why do you disagree?", "Tell me why you disagree.");
    replies.put(normalize("No"), whyDisagree);
    replies.put(normalize("Nah"), whyDisagree);
    replies.put(normalize("Nope"), whyDisagree);
    replies.put(normalize("Disagree"), whyDisagree);
    replies.put(normalize("I disagree"), whyDisagree);
    List<String> whyAgree = Arrays.asList("Why do you agree?", "Tell me why you agree.");
    replies.put(normalize("Yes"), whyAgree);
    replies.put(normalize("Yeah"), whyAgree);
    List<String> loveYouToo = Arrays.asList("I love you too!");
    replies.put(normalize("I love you"), whyAgree);
    questionResponses = Arrays.asList("I don't know.", "Definitely!", "No", "Yes",
        "Hmmmm, no idea", "Never");
    defaultResponses = Arrays.asList("Tell me more", "What do you mean by that?",
        "Can you elaborate?", "What do you feel when you say that?");
  }

  public String getReply(String message) {
    String normalized = normalize(message);
    List<String> repliesToMessage = replies.get(normalized);
    if (repliesToMessage == null || repliesToMessage.isEmpty()) {
      if (message.contains("?")) {
        return questionResponses.get(random.nextInt(questionResponses.size()));
      } else {
        return defaultResponses.get(random.nextInt(defaultResponses.size()));
      }
    } else {
      return repliesToMessage.get(random.nextInt(repliesToMessage.size()));
    }
  }

  private String normalize(String text) {
    return text
        .replace(" ", "")
        .replace("'", "")
        .replace("\"", "")
        .replace("!", "")
        .toUpperCase();
  }
}
