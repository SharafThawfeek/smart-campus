USE smart_campus_db;

-- 1. Insert Resources
INSERT INTO resources (name, type, capacity, location, status) VALUES
('Lecture Hall A', 'ROOM', 150, 'Building 1, Floor 1', 'AVAILABLE'),
('Computer Lab 1', 'LAB', 30, 'Building 2, Floor 2', 'AVAILABLE'),
('Conference Room B', 'ROOM', 15, 'Building 1, Floor 3', 'AVAILABLE'),
('Projector Pro-X', 'EQUIPMENT', NULL, 'IT Storage', 'AVAILABLE'),
('Chemistry Lab A', 'LAB', 20, 'Science Wing, Floor 1', 'MAINTENANCE'),
('Microscope M-100', 'EQUIPMENT', NULL, 'Biology Lab', 'IN_USE');

-- 2. Insert Bookings
INSERT INTO bookings (start_time, end_time, purpose, status, resource_id, user_id, admin_reason) VALUES
(DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), 'Computer Science 101 Lecture', 'APPROVED', 1, 1, 'Approved by admin'),
(DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 'Study Group', 'PENDING', 3, 1, NULL),
(DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), 'Seminar Preps', 'REJECTED', 2, 3, 'Lab is booked for maintenance on that day'),
(DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 3 HOUR), 'Presentations', 'APPROVED', 4, 3, 'Equipment allocated');

-- 3. Insert Incident Tickets
INSERT INTO incident_tickets (category, description, priority, status, resource_id, user_id, assigned_technician_id) VALUES
('HARDWARE', 'Projector in Lecture Hall A has a broken bulb.', 'HIGH', 'OPEN', 1, 1, NULL),
('SOFTWARE', 'Computers in Lab 1 are not connecting to the network.', 'MEDIUM', 'IN_PROGRESS', 2, 3, 2),
('PLUMBING', 'Leaking pipe in Science Wing bathroom.', 'LOW', 'RESOLVED', NULL, 1, 2);

-- 4. Insert Comments on Tickets
INSERT INTO comments (content, created_at, ticket_id, user_id) VALUES
('I will check this right away.', NOW(), 2, 2),
('Confirmed the issue, waiting on replacement parts.', NOW(), 1, 2),
('Thanks for resolving it quickly!', NOW(), 3, 1);

-- 5. Insert Notifications
INSERT INTO notifications (message, is_read, created_at, user_id) VALUES
('Your booking for Computer Science 101 Lecture has been APPROVED.', b'0', NOW(), 1),
('Your booking for Seminar Preps has been REJECTED. Reason: Lab is booked for maintenance on that day.', b'0', NOW(), 3),
('New incident reported for Computers in Lab 1.', b'0', NOW(), 2),
('Your ticket regarding Leaking pipe has been marked as RESOLVED.', b'1', NOW(), 1);
