from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List
from contextlib import asynccontextmanager
from sqlalchemy.orm import Session
import logging

from database import init_db, get_db, BookDB

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("book_api")

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    logger.info("Initializing database...")
    init_db()
    logger.info("Database initialized")
    yield
    # Shutdown (если нужно что-то закрыть)

app = FastAPI(lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class BookCreate(BaseModel):
    title: str
    year: int
    author: str
    status: str
    dateAdded: str
    note: str

class BookResponse(BaseModel):
    id: int
    title: str
    year: int
    author: str
    status: str
    dateAdded: str
    note: str

    class Config:
        from_attributes = True

class BookUpdate(BaseModel):
    title: str
    year: int
    author: str
    status: str
    dateAdded: str
    note: str

@app.get("/books", response_model=List[BookResponse])
def get_books(db: Session = Depends(get_db)):
    books = db.query(BookDB).all()
    logger.info(f"GET /books count={len(books)}")
    return books

@app.post("/books", response_model=BookResponse)
def create_book(book: BookCreate, db: Session = Depends(get_db)):
    logger.info(f"POST /books payload={book.model_dump_json()}")
    db_book = BookDB(
        title=book.title,
        year=book.year,
        author=book.author,
        status=book.status,
        dateAdded=book.dateAdded,
        note=book.note
    )
    db.add(db_book)
    db.commit()
    db.refresh(db_book)
    logger.info(f"Created book id={db_book.id}")
    return db_book

@app.get("/books/{book_id}", response_model=BookResponse)
def get_book(book_id: int, db: Session = Depends(get_db)):
    logger.info(f"GET /books/{book_id}")
    book = db.query(BookDB).filter(BookDB.id == book_id).first()
    if not book:
        raise HTTPException(status_code=404, detail="Book not found")
    return book

@app.put("/books/{book_id}", response_model=BookResponse)
def update_book(book_id: int, book: BookUpdate, db: Session = Depends(get_db)):
    logger.info(f"PUT /books/{book_id} payload={book.model_dump_json()}")
    db_book = db.query(BookDB).filter(BookDB.id == book_id).first()
    if not db_book:
        raise HTTPException(status_code=404, detail="Book not found")
    
    db_book.title = book.title
    db_book.year = book.year
    db_book.author = book.author
    db_book.status = book.status
    db_book.dateAdded = book.dateAdded
    db_book.note = book.note
    
    db.commit()
    db.refresh(db_book)
    logger.info(f"Updated book id={book_id}")
    return db_book

@app.delete("/books/{book_id}")
def delete_book(book_id: int, db: Session = Depends(get_db)):
    logger.info(f"DELETE /books/{book_id}")
    db_book = db.query(BookDB).filter(BookDB.id == book_id).first()
    if not db_book:
        raise HTTPException(status_code=404, detail="Book not found")
    
    db.delete(db_book)
    db.commit()
    logger.info(f"Deleted book id={book_id}")
    return {"message": "Book deleted"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5001)
