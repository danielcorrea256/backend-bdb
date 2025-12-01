-- Insert test users
INSERT INTO users (id, username, full_name, email) VALUES
(1, 'admin_dev', 'Administrator Dev', 'admin@rockchallenge.com'),
(2, 'qa_lead', 'QA Lead User', 'qa@rockchallenge.com'),
(3, 'junior_dev', 'Junior Developer', 'junior@rockchallenge.com');

-- Insert request types
INSERT INTO request_types (id, name, description) VALUES
(1, 'ACCESS', 'Request for system access'),
(2, 'DEPLOYMENT', 'Request for deployment to production'),
(3, 'BUDGET', 'Budget approval request');
