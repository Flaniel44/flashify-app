-- Teachers
CREATE TABLE IF NOT EXISTS teachers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Students
CREATE TABLE IF NOT EXISTS students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID NOT NULL REFERENCES teachers(id),
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Word banks
CREATE TABLE IF NOT EXISTS word_banks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES students(id),
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Words
CREATE TABLE IF NOT EXISTS words (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    word_bank_id UUID NOT NULL REFERENCES word_banks(id),
    word VARCHAR(100) NOT NULL,
    translation VARCHAR(100),
    hint TEXT,
    notes TEXT
);

-- Sessions
CREATE TABLE IF NOT EXISTS sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID NOT NULL REFERENCES teachers(id),
    student_id UUID NOT NULL REFERENCES students(id),
    word_bank_id UUID NOT NULL REFERENCES word_banks(id),
    status VARCHAR(20) DEFAULT 'waiting' CHECK (status IN ('waiting', 'active', 'completed')),
    current_word_index INT DEFAULT 0,
    current_turn VARCHAR(10) CHECK (current_turn IN ('teacher', 'student')),
    invite_token UUID DEFAULT gen_random_uuid(),
    word_revealed BOOLEAN DEFAULT FALSE,
    hint_revealed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- Session words
CREATE TABLE IF NOT EXISTS session_words (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES sessions(id),
    word_id UUID NOT NULL REFERENCES words(id),
    revealed_by VARCHAR(10) CHECK (revealed_by IN ('teacher', 'student')),
    hint_used BOOLEAN DEFAULT FALSE,
    revealed_at TIMESTAMP DEFAULT NOW()
);