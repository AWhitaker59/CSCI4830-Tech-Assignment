let nextCardID = 0;
document.addEventListener("DOMContentLoaded", () => {
    var contentArray = [];

    document.getElementById("save_card").addEventListener("click", () => {
        addFlashcard();
    });

    document.getElementById("show_card_box").addEventListener("click", () => {
        document.getElementById("create_card").style.display = "block";
    });

    document.getElementById("close_card_box").addEventListener("click", () => {
        document.getElementById("create_card").style.display = "none";
    });

    const flashcards = document.getElementById("flashcards");

//function to actually build the cards 
    const flashcardMaker = (text, delThisIndex, cardID) => {
        const flashcard = document.createElement("div");
        const question = document.createElement('h2');
        const answer = document.createElement('h2');
        const del = document.createElement('i');

        flashcard.className = 'flashcard';
		
		flashcard.setAttribute("data-cardid", cardID);
		
        question.setAttribute("style", "border-top:1px solid red; padding: 15px; margin-top:30px");
        question.textContent = text.my_question;

        answer.setAttribute("style", "text-align:center; display:none; color:red");
        answer.textContent = text.my_answer;

        del.className = "fas fa-minus";
        del.addEventListener("click", () => {
            deleteFlashcard(delThisIndex);
        });

        flashcard.appendChild(question);
        flashcard.appendChild(answer);
        flashcard.appendChild(del);

        flashcard.addEventListener("click", () => {
            toggleAnswerDisplay(answer);
        });

        flashcards.appendChild(flashcard);
    }

//function to get the current cards from DB
    const loadFlashcardsFromDatabase = () => {
        $.ajax({
            type: "GET",
            url: "/Initial_Hello_World/FlashcardJava",
            contentType: "application/json",
            success: function (response) {
                if (response.success) {
                    const flashcardsArray = response.flashcards;

                    flashcardsArray.forEach((flashcard) => {
						const cardID = flashcard.ID; 
						nextCardID = Math.max(nextCardID, cardID);
                        const question = flashcard.Question;
                        const answer = flashcard.Answer;

                        contentArray.push({
                            'my_question': question,
                            'my_answer': answer,
                            'cardID': cardID
                        });

                        flashcardMaker({
                            'my_question': question,
                            'my_answer': answer,
                            'cardID': cardID
                        });
                    });
                } else {
                    console.error("Failed to load flashcards from the database.");
                }
            },
            error: function (errorThrown) {
                console.error("Failed to load flashcards from the database. Error: " + errorThrown);
            }
        });
    };

    loadFlashcardsFromDatabase();

//fucntion to add a flashcard on pressing the save button
    const addFlashcard = () => {
		nextCardID ++;
		
        const questionTextarea = document.querySelector("textarea[name='question']");
        const answerTextarea = document.querySelector("textarea[name='answer']");
        console.log("Question:", questionTextarea.value);
        console.log("Answer:", answerTextarea.value);

        const data = {
            question: questionTextarea.value,
            answer: answerTextarea.value,
            cardID: nextCardID
        };

        $.ajax({
            type: "POST",
            url: "/Initial_Hello_World/FlashcardJava",
            data: JSON.stringify(data),
            contentType: "application/json",
            success: function (response) {
                if (response.success) {
                    const question = questionTextarea.value;
                    const answer = answerTextarea.value;
                    contentArray.push({
                        'my_question': question,
                        'my_answer': answer,
                        'cardID': nextCardID
                    });
                    flashcardMaker({
                        'my_question': question,
                        'my_answer': answer,
                        'cardID': nextCardID
                    });
                    document.querySelector("textarea[name='question']").value = "";
                    document.querySelector("textarea[name='answer']").value = "";
                } else {
                    console.error("Failed to add flashcard.");
                }
            },
            error: function (errorThrown) {
                console.error("Failed to add flashcard. Error: " + errorThrown);
            }
        });
    };

//function to delete cards
    const deleteFlashcard = (index) => {
        // Handle flashcard deletion here
        // You should send a request to your servlet to delete the flashcard from the database
        // After that, remove the flashcard from contentArray and the DOM
    };

//function to show/hide answer
    const toggleAnswerDisplay = (answerElement) => {
        if (answerElement.style.display === "none") {
            answerElement.style.display = "block";
        } else {
            answerElement.style.display = "none";
        }
    };
});
