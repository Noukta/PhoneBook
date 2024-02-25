package phonebook

import java.io.File
import java.util.*
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds

const val DIRECTORY = "C:\\Users\\no9ta\\IdeaProjects\\Phone Book (Kotlin)\\directory.txt"
const val SORTED_DIR = "C:\\Users\\no9ta\\IdeaProjects\\Phone Book (Kotlin)\\sorted_directory.txt"
const val FIND = "C:\\Users\\no9ta\\IdeaProjects\\Phone Book (Kotlin)\\find.txt"

fun Long.formatDurationTime() =
    milliseconds.toComponents { _, minutes, seconds, nanoseconds ->
        String.format(
            Locale.getDefault(),
            "%02d min. %02d sec. %02d ms",
            minutes,
            seconds,
            nanoseconds / 1_000_000,
        )
    }


class PhoneBook(fileName: String) {
    private val contacts = mutableListOf<Contact>()
    private val contactsHashMap = hashMapOf<String, String>()

    init {
        loadContacts(fileName)
    }

    data class Contact(
        var number: String = "",
        var name: String = ""
    ) {
        constructor(input: String) : this() {
            input.split(" ").also {
                this.number = it[0]
                this.name = it.drop(1).joinToString(" ")
            }
        }

        override fun toString(): String {
            return "$number $name"
        }

    }

    private fun loadContacts(fileName: String) {
        val file = File(fileName)
        val scanner = Scanner(file)
        while (scanner.hasNextLine()) {
            contacts.add(Contact(scanner.nextLine()))
        }
    }

    fun listToHashMap() {
        for (contact in contacts) {
            contactsHashMap[contact.name] = contact.name
        }
    }

    fun save(fileName: String) {
        val file = File(fileName)
        for (contact in contacts) {
            file.appendText("$contact\n")
        }
    }

    fun loadNames(fileName: String): List<String> {
        val file = File(fileName)
        val scanner = Scanner(file)
        val names = mutableListOf<String>()
        while (scanner.hasNextLine()) {
            names.add(scanner.nextLine())
        }
        return names.toList()
    }

    fun linearSearch(names: List<String>): Int {
        var found = 0
        for (contact in contacts) {
            for (name in names) {
                if (name == contact.name)
                    found++
            }
        }
        return found
    }

    fun bubbleSort(timeLimit: Long): Long {
        val start = System.currentTimeMillis()
        var swapped = true
        while (swapped) {
            swapped = false
            for (i in 1 until contacts.size) {
                if (contacts[i - 1].name > contacts[i].name) {
                    val temp = contacts[i - 1]
                    contacts[i - 1] = contacts[i]
                    contacts[i] = temp
                    swapped = true
                }
                val currentDuration = System.currentTimeMillis() - start
                if (currentDuration > timeLimit) return currentDuration
            }
        }
        return System.currentTimeMillis() - start
    }

    private fun jumpSearch(name: String): Int {
        var curr = 0
        var prev = 0
        val step = floor(sqrt(contacts.size.toDouble())).toInt()
        while (contacts[curr].name < name) {
            if (curr == contacts.size) return 0
            prev = curr
            curr = min(curr + step, contacts.size)
        }
        while (contacts[curr].name > name) {
            curr--
            if (curr <= prev) return 0
        }
        if (contacts[curr].name == name)
            return 1
        return 0
    }

    fun jumpSearchAll(names: List<String>): Int {
        if (contacts.isEmpty()) return -1
        var found = 0
        for (name in names) {
            found += jumpSearch(name)
        }
        return found
    }

    fun quickSort(begin: Int = 0, end: Int = contacts.lastIndex) {
        if (begin < end) {
            val partitionIndex = partition(begin, end)
            quickSort(begin, partitionIndex - 1)
            quickSort(partitionIndex, end)
        }
    }

    private fun partition(begin: Int, end: Int): Int {
        val pivot = contacts[end]
        var i = begin - 1
        for (j in begin until end) {
            if (contacts[j].name <= pivot.name) {
                i++
                val swapTemp = contacts[i]
                contacts[i] = contacts[j]
                contacts[j] = swapTemp
            }
        }
        val swapTemp = contacts[i + 1]
        contacts[i + 1] = contacts[end]
        contacts[end] = swapTemp
        return i + 1
    }

    private fun binarySearch(name: String): Int {
        var (low, high) = listOf(0, contacts.lastIndex)
        while (low <= high) {
            val mid = low + ((high - low) / 2)
            if (contacts[mid].name < name)
                low = mid + 1
            else if (contacts[mid].name > name)
                high = mid - 1
            else if (contacts[mid].name == name)
                return 1
        }
        return 0
    }

    fun binarySearchAll(names: List<String>): Int {
        if (contacts.isEmpty()) return -1
        var found = 0
        for (name in names) {
            found += binarySearch(name)
        }
        return found
    }

    fun searchByKeys(names: List<String>): Int {
        var found = 0
        for (name in names) {
            found += if (contactsHashMap.containsKey(name)) 1 else 0
        }
        return found
    }
}

fun main() {
    val phoneBook = PhoneBook(DIRECTORY)
    val names = phoneBook.loadNames(FIND)
    var start = System.currentTimeMillis()
    println("Start searching (linear search)...")
    var found = phoneBook.linearSearch(names)
    var end = System.currentTimeMillis()
    val linearSearchDuration = end - start
    println("Found $found / ${names.size}. Time taken: ${linearSearchDuration.formatDurationTime()}.")
    println()
    println("Start searching (bubble sort + jump search)...")
    val bubbleSortDuration = phoneBook.bubbleSort(linearSearchDuration * 10)
    //phoneBook.save(SORTED_DIR)
    if (bubbleSortDuration < linearSearchDuration * 10) {
        println()
        start = System.currentTimeMillis()
        found = phoneBook.jumpSearchAll(names)
        end = System.currentTimeMillis()
        val jumpSearchDuration = end - start
        println("Found $found / ${names.size}. Time taken: ${(bubbleSortDuration + jumpSearchDuration).formatDurationTime()}.")
        println("Sorting time: ${bubbleSortDuration.formatDurationTime()}.")
        println("Searching time: ${jumpSearchDuration.formatDurationTime()}.")
    } else {
        println("Found $found / ${names.size}. Time taken: ${(bubbleSortDuration + linearSearchDuration).formatDurationTime()}.")
        print("Sorting time: ${bubbleSortDuration.formatDurationTime()}.")
        println(" - STOPPED, moved to linear search")
        println("Searching time: ${linearSearchDuration.formatDurationTime()}.")
    }
    println()
    val phoneBook2 = PhoneBook(DIRECTORY)
    println("Start searching (quick sort + binary search)...")
    start = System.currentTimeMillis()
    phoneBook2.quickSort()
    val quickSortDuration = System.currentTimeMillis() - start
    //phoneBook2.save(SORTED_DIR)
    start = System.currentTimeMillis()
    found = phoneBook2.binarySearchAll(names)
    end = System.currentTimeMillis()
    val binarySearchDuration = end - start
    println("Found $found / ${names.size}. Time taken: ${(quickSortDuration + binarySearchDuration).formatDurationTime()}.")
    println("Sorting time: ${quickSortDuration.formatDurationTime()}.")
    println("Searching time: ${binarySearchDuration.formatDurationTime()}.")

    println()
    val phoneBook3 = PhoneBook(DIRECTORY)
    println("Start searching (hash table)...")
    start = System.currentTimeMillis()
    phoneBook3.listToHashMap()
    val hashMapCreatingDuration = System.currentTimeMillis() - start
    start = System.currentTimeMillis()
    found = phoneBook3.searchByKeys(names)
    end = System.currentTimeMillis()
    val searchByKeysDuration = end - start
    println("Found $found / ${names.size}. Time taken: ${(hashMapCreatingDuration + searchByKeysDuration).formatDurationTime()}.")
    println("Creating time: ${hashMapCreatingDuration.formatDurationTime()}.")
    println("Searching time: ${searchByKeysDuration.formatDurationTime()}.")
}
