import java.util.*;

/**
 * ContactDirectory is a simple command-line application that manages a list of contacts.
 * It provides autocomplete functionality using a Trie (prefix tree) structure.
 * 
 * Features include:
 * - Adding a new contact.
 * - Autocomplete search based on a name prefix.
 * - Listing all contacts.
 * - Deleting a contact.
 */
public class ContactDirectory {

    // Represents a node in the Trie.
    static class TrieNode {
        Map<Character, TrieNode> children; // Map each character to its child node.
        boolean isEndOfName;               // Indicates if a complete contact name ends at this node.
        List<String> nameSuggestions;      // Holds contact names sharing this prefix.

        public TrieNode() {
            children = new HashMap<>();
            isEndOfName = false;
            nameSuggestions = new ArrayList<>();
        }
    }

    // A simple Trie for storing contact names and supporting prefix search.
    static class AutocompleteTrie {
        private TrieNode root;

        public AutocompleteTrie() {
            root = new TrieNode();
        }

        // Inserts a contact name into the trie.
        public void insertName(String contactName) {
            TrieNode currentNode = root;
            String lowerCaseName = contactName.toLowerCase();
            for (char letter : lowerCaseName.toCharArray()) {
                currentNode.children.putIfAbsent(letter, new TrieNode());
                currentNode = currentNode.children.get(letter);
                // Only add if not already included to avoid duplicates in suggestions.
                if (!currentNode.nameSuggestions.contains(contactName)) {
                    currentNode.nameSuggestions.add(contactName);
                }
            }
            currentNode.isEndOfName = true;
        }

        // Removes a contact name from the trie.
        public boolean removeName(String contactName) {
            return removeNameRecursively(root, contactName.toLowerCase(), 0);
        }

        private boolean removeNameRecursively(TrieNode current, String contactName, int index) {
            if (index == contactName.length()) {
                if (!current.isEndOfName) {
                    return false;
                }
                current.isEndOfName = false;
                return current.children.isEmpty();
            }
            char letter = contactName.charAt(index);
            TrieNode childNode = current.children.get(letter);
            if (childNode == null) {
                return false;
            }
            boolean shouldDeleteChild = removeNameRecursively(childNode, contactName, index + 1);
            if (shouldDeleteChild) {
                current.children.remove(letter);
                return current.children.isEmpty() && !current.isEndOfName;
            }
            // Remove this name from the suggestions list at this node.
            childNode.nameSuggestions.removeIf(name -> name.equalsIgnoreCase(contactName));
            return false;
        }

        // Searches for contact names that start with the given prefix.
        public List<String> searchByPrefix(String prefix) {
            TrieNode currentNode = root;
            String lowerPrefix = prefix.toLowerCase();
            for (char letter : lowerPrefix.toCharArray()) {
                if (!currentNode.children.containsKey(letter)) {
                    return new ArrayList<>();
                }
                currentNode = currentNode.children.get(letter);
            }
            return currentNode.nameSuggestions;
        }
    }

    // Instance variables for our contact directory.
    private AutocompleteTrie autoCompleteTrie;
    private Set<String> contactSet;

    // Constructor initializes our data structures.
    public ContactDirectory() {
        autoCompleteTrie = new AutocompleteTrie();
        contactSet = new HashSet<>();
    }

    // Adds a new contact if it does not already exist.
    public void addContact(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Contact name cannot be empty.");
            return;
        }
        if (contactSet.add(name)) { // Only add if it's a new contact.
            autoCompleteTrie.insertName(name);
            System.out.println("Successfully added contact: " + name);
        } else {
            System.out.println("Contact already exists: " + name);
        }
    }

    // Displays autocomplete suggestions for a given name prefix.
    public void searchContacts(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            System.out.println("Please enter a valid prefix to search.");
            return;
        }
        List<String> suggestions = autoCompleteTrie.searchByPrefix(prefix);
        if (suggestions.isEmpty()) {
            System.out.println("No contacts found starting with \"" + prefix + "\".");
        } else {
            System.out.println("Autocomplete suggestions for \"" + prefix + "\":");
            for (String suggestion : suggestions) {
                System.out.println("  - " + suggestion);
            }
        }
    }

    // Lists all contacts in the directory.
    public void listAllContacts() {
        if (contactSet.isEmpty()) {
            System.out.println("No contacts available in the directory.");
        } else {
            System.out.println("Contact List:");
            for (String name : contactSet) {
                System.out.println("  - " + name);
            }
        }
    }

    // Deletes a contact from the directory.
    public void deleteContact(String name) {
        if (contactSet.remove(name)) {
            autoCompleteTrie.removeName(name);
            System.out.println("Contact removed: " + name);
        } else {
            System.out.println("Contact not found: " + name);
        }
    }

    // Main method to run the application.
    public static void main(String[] args) {
        ContactDirectory directory = new ContactDirectory();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        // Add some sample contacts.
        directory.addContact("Alice Johnson");
        directory.addContact("Albert Smith");
        directory.addContact("Alex Brown");

        while (running) {
            System.out.println("\n===== Contact Directory Menu =====");
            System.out.println("1. Add a Contact");
            System.out.println("2. Search Contacts (Autocomplete)");
            System.out.println("3. List All Contacts");
            System.out.println("4. Delete a Contact");
            System.out.println("5. Exit");
            System.out.print("Enter your choice (1-5): ");

            int userChoice = 0;
            try {
                userChoice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input. Please enter a number from 1 to 5.");
                continue;
            }

            switch (userChoice) {
                case 1:
                    System.out.print("Enter contact name: ");
                    String newContact = scanner.nextLine();
                    directory.addContact(newContact);
                    break;
                case 2:
                    System.out.print("Enter name prefix to search: ");
                    String prefix = scanner.nextLine();
                    directory.searchContacts(prefix);
                    break;
                case 3:
                    directory.listAllContacts();
                    break;
                case 4:
                    System.out.print("Enter contact name to delete: ");
                    String contactToDelete = scanner.nextLine();
                    directory.deleteContact(contactToDelete);
                    break;
                case 5:
                    running = false;
                    System.out.println("Thank you for using Contact Directory. Goodbye!");
                    break;
                default:
                    System.out.println("Please select a valid option (1-5).");
            }
        }
        scanner.close();
    }
}
