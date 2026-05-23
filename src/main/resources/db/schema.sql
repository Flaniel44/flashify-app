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
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Student word banks (many-to-many)
CREATE TABLE IF NOT EXISTS student_word_banks (
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    word_bank_id UUID NOT NULL REFERENCES word_banks(id) ON DELETE CASCADE,
    PRIMARY KEY (student_id, word_bank_id)
);

-- Words
CREATE TABLE IF NOT EXISTS words (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    word_bank_id UUID NOT NULL REFERENCES word_banks(id) ON DELETE CASCADE,
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
    word_bank_id UUID NOT NULL REFERENCES word_banks(id) ON DELETE CASCADE,
    status VARCHAR(20) DEFAULT 'waiting' CHECK (status IN ('waiting', 'active', 'completed')),
    session_type VARCHAR(20) DEFAULT 'alternating' CHECK (session_type IN ('alternating', 'teacher_only', 'student_only')),
    current_word_index INT DEFAULT 0,
    current_turn VARCHAR(10) CHECK (current_turn IN ('teacher', 'student')),
    invite_token UUID DEFAULT gen_random_uuid(),
    word_revealed BOOLEAN DEFAULT FALSE,
    hint_revealed BOOLEAN DEFAULT FALSE,
    shuffled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- Session words
CREATE TABLE IF NOT EXISTS session_words (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    word_id UUID NOT NULL REFERENCES words(id) ON DELETE CASCADE,
    revealed_by VARCHAR(10) CHECK (revealed_by IN ('teacher', 'student')),
    hint_used BOOLEAN DEFAULT FALSE,
    revealed_at TIMESTAMP DEFAULT NOW()
);

-- Session word order (for shuffled sessions)
CREATE TABLE IF NOT EXISTS session_word_order (
    session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    word_id UUID NOT NULL REFERENCES words(id) ON DELETE CASCADE,
    position INT NOT NULL,
    PRIMARY KEY (session_id, position)
);