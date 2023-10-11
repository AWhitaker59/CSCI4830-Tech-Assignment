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
    const flashcardMaker = (text, cardID) => {
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

        del.className = "fas fa-trash";
        del.addEventListener("click", () => {
            deleteFlashcard(cardID);
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
    const getStoredCards = () => {
        $.ajax({
            type: "GET",
            url: "/Initial_Hello_World/FlashcardJava",
            contentType: "application/json",
            success: function (response) {
                if (response.success) {
                    const flashcardsArray = response.flashcards;

                    flashcardsArray.forEach((flashcard) => {
						const cardID = flashcard.cardID; 
                        const question = flashcard.Question;
                        const answer = flashcard.Answer;

                        const text = {
                        'my_question': question,
                        'my_answer': answer
                   		};

                    	flashcardMaker(text, cardID);
                });
                } else {
                    console.error("Failed get flashcards");
                }
            },
            error: function (errorThrown) {
                console.error("Failed to get flashcards: " + errorThrown);
            }
        });
    };

    getStoredCards();

//function to add a flashcard on pressing the save button
    const addFlashcard = () => {
		
        const questionTextarea = document.querySelector("textarea[name='question']");
        const answerTextarea = document.querySelector("textarea[name='answer']");
        const data = {
            question: questionTextarea.value,
            answer: answerTextarea.value,
        };

        $.ajax({
            type: "POST",
            url: "/Initial_Hello_World/FlashcardJava",
            data: JSON.stringify(data),
            contentType: "application/json",
            success: function (response) {
                if (response.success) {
					const cardID = response.cardID;
                    const question = questionTextarea.value;
                    const answer = answerTextarea.value;

                    const text = {
                        'my_question': question,
                        'my_answer': answer
                   	};

                    flashcardMaker(text, cardID);
                
                    document.querySelector("textarea[name='question']").value = "";
                    document.querySelector("textarea[name='answer']").value = "";
                } else {
                    console.error("Failed to add flashcard.");
                }
            },
            error: function (errorThrown) {
                console.error("Failed to add flashcard: " + errorThrown);
            }
        });
    };

//function to delete cards
    const deleteFlashcard = (cardID) => {
		console.log("cardID:" + cardID);
        $.ajax({
	        type: "POST",
	        url: "/Initial_Hello_World/FlashcardDeletion", 
	        data: JSON.stringify({ cardID: cardID }),
	        contentType: "application/json",
	        success: function (response) {
	            if (response.success) {
	                const flashcardElement = document.querySelector(`.flashcard[data-cardid="${cardID}"]`);
	                if (flashcardElement) {
	                    flashcardElement.remove();
	                }
	            } else {
	                console.error("Card could not be deleted");
	            }
	        },
	        error: function (errorThrown) {
	            console.error("Card could not be deleted: " + errorThrown);
	        }
    	});
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
